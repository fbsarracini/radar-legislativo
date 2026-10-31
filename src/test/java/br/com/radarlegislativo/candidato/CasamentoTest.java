package br.com.radarlegislativo.candidato;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.deputadoFederal;
import static org.assertj.core.api.Assertions.assertThat;

class CasamentoTest {

    private static Candidato candidato(String sq, int ano, String nome, String urna) {
        return new Candidato(sq, ano, nome, urna, "DEPUTADO FEDERAL", "SP", null, "XYZ", "10", "ELEITO");
    }

    @Test
    @DisplayName("deve classificar a confiança pelo nome")
    void deveClassificarConfianca() {
        Candidato c = deputadoFederal("1", "JOÃO PEREIRA DA SILVA", "JOÃO SILVA");

        assertThat(Casamento.classificar(c, "João Silva")).contains(Vinculo.Confianca.NOME_URNA_EXATO);
        assertThat(Casamento.classificar(c, "João Pereira da Silva")).contains(Vinculo.Confianca.NOME_CIVIL_EXATO);
        assertThat(Casamento.classificar(c, "Pereira Silva")).contains(Vinculo.Confianca.NOME_PARCIAL);
        assertThat(Casamento.classificar(c, "Maria Santos")).isEmpty();
    }

    @Test
    @DisplayName("deve sugerir, em cada eleição, só o grau de confiança mais alto")
    void deveSugerirSoOMelhorDeCadaEleicao() {
        Candidato exato2026 = candidato("1", 2026, "JOÃO PEREIRA DA SILVA", "JOÃO SILVA");
        Candidato parcial2026 = candidato("2", 2026, "JOÃO SILVA SAURO", "SAURO");
        Candidato parcial2022 = candidato("3", 2022, "JOÃO DA SILVA NETO", "NETO");

        assertThat(Casamento.sugerir("João Silva", List.of(exato2026, parcial2026, parcial2022)))
                .containsOnlyKeys(exato2026, parcial2022)
                .containsEntry(parcial2022, Vinculo.Confianca.NOME_PARCIAL);
    }

    @Test
    @DisplayName("não deve sugerir casamento parcial ambíguo demais")
    void naoDeveSugerirParcialAmbiguo() {
        List<Candidato> anas = List.of(
                candidato("1", 2026, "ANA PAULA SOUZA", "SOUZA"),
                candidato("2", 2026, "ANA PAULA LIMA", "LIMA"),
                candidato("3", 2026, "ANA PAULA REIS", "REIS"),
                candidato("4", 2026, "ANA PAULA MOTA", "MOTA"));

        assertThat(Casamento.sugerir("Ana Paula", anas)).isEmpty();
        assertThat(Casamento.sugerir("Ana Paula", anas.subList(0, Casamento.MAX_PARCIAIS))).hasSize(Casamento.MAX_PARCIAIS);
    }
}
