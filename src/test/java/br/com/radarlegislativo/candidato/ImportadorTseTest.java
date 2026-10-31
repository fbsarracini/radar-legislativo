package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Esfera;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ImportadorTseTest {

    private static final String CABECALHO =
            "\"ANO_ELEICAO\";\"NR_TURNO\";\"SG_UF\";\"NM_UE\";\"DS_CARGO\";\"SQ_CANDIDATO\";\"NR_CANDIDATO\";\"NM_CANDIDATO\";\"NM_URNA_CANDIDATO\";\"SG_PARTIDO\";\"DS_SIT_TOT_TURNO\"";

    private static Map<String, Candidato> ler(String... linhas) throws Exception {
        String csv = CABECALHO + "\n" + String.join("\n", linhas);
        Map<String, Candidato> destino = new LinkedHashMap<>();
        ImportadorTse.lerStream(new ByteArrayInputStream(csv.getBytes(StandardCharsets.ISO_8859_1)), 2026, destino);
        return destino;
    }

    @Test
    @DisplayName("deve ler todos os candidatos, em Latin-1, deduplicando o segundo turno")
    void deveLerTodosOsCandidatosEDeduplicarSegundoTurno() throws Exception {
        Map<String, Candidato> destino = ler(
                "\"2026\";\"1\";\"SP\";\"SÃO PAULO\";\"DEPUTADO FEDERAL\";\"250001\";\"4010\";\"JOÃO DA SILVA\";\"JOÃO SILVA\";\"XYZ\";\"ELEITO POR QP\"",
                "\"2026\";\"1\";\"SP\";\"SÃO PAULO\";\"DEPUTADO FEDERAL\";\"250002\";\"4011\";\"MARIA SOUZA\";\"MARIA\";\"XYZ\";\"SUPLENTE\"",
                "\"2026\";\"1\";\"MG\";\"MINAS GERAIS\";\"GOVERNADOR\";\"250003\";\"10\";\"PEDRO \"\"PEDRINHO\"\" ALVES\";\"PEDRINHO\";\"ABC\";\"2º TURNO\"",
                "\"2026\";\"2\";\"MG\";\"MINAS GERAIS\";\"GOVERNADOR\";\"250003\";\"10\";\"PEDRO \"\"PEDRINHO\"\" ALVES\";\"PEDRINHO\";\"ABC\";\"ELEITO\"");

        assertThat(destino).containsOnlyKeys("250001", "250002", "250003");
        Candidato joao = destino.get("250001");
        assertThat(joao.nome()).isEqualTo("JOÃO DA SILVA");
        assertThat(joao.esferaDoMandato()).isEqualTo(Esfera.CAMARA_FEDERAL);
        assertThat(joao.eleito()).isTrue();
        assertThat(destino.get("250002").eleito()).isFalse();
        Candidato pedro = destino.get("250003");
        assertThat(pedro.nome()).isEqualTo("PEDRO \"PEDRINHO\" ALVES");
        assertThat(pedro.situacao()).isEqualTo("ELEITO");
        assertThat(pedro.eleito()).isTrue();
    }

    @Test
    @DisplayName("deve ignorar linha sem SQ_CANDIDATO")
    void deveIgnorarLinhaSemSq() throws Exception {
        Map<String, Candidato> destino = ler(
                "\"2026\";\"1\";\"SP\";\"SÃO PAULO\";\"DEPUTADO FEDERAL\";\"\";\"4010\";\"JOÃO\";\"JOÃO\";\"XYZ\";\"ELEITO\"");

        assertThat(destino).isEmpty();
    }
}
