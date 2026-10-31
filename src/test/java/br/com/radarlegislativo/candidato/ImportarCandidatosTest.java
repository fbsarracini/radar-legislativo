package br.com.radarlegislativo.candidato;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static br.com.radarlegislativo.TestFixtures.deputadoFederal;
import static br.com.radarlegislativo.TestFixtures.props;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportarCandidatosTest {

    @TempDir
    Path diretorio;

    @Mock
    private ImportadorTse importador;
    @Mock
    private CandidatoRepository candidatoRepository;
    @Mock
    private VincularParlamentares vincularParlamentares;

    private ImportarCandidatos importarCandidatos;

    @BeforeEach
    void setUp() {
        importarCandidatos = new ImportarCandidatos(importador, candidatoRepository, vincularParlamentares, props(diretorio.toString()));
    }

    @Test
    @DisplayName("deve baixar do TSE e substituir os candidatos do ano")
    void deveBaixarDoTseESubstituirAno() {
        List<Candidato> candidatos = List.of(deputadoFederal("1", "JOÃO DA SILVA", "JOÃO SILVA"));
        when(importador.importar(2026)).thenReturn(candidatos);

        int total = importarCandidatos.execute(new ImportarCandidatosRequest(2026, null));

        assertThat(total).isEqualTo(1);
        verify(candidatoRepository).substituirAno(2026, candidatos);
        verify(vincularParlamentares).executeTodos();
    }

    @Test
    @DisplayName("deve ler arquivo local dentro do diretório de dados")
    void deveLerArquivoDentroDoDiretorio() throws Exception {
        Path zip = Files.createFile(diretorio.resolve("consulta_cand_2026.zip"));
        when(importador.importarArquivo(zip, 2026)).thenReturn(List.of());

        int total = importarCandidatos.execute(new ImportarCandidatosRequest(2026, "consulta_cand_2026.zip"));

        assertThat(total).isZero();
    }

    @Test
    @DisplayName("deve recusar arquivo inexistente")
    void deveRecusarArquivoInexistente() {
        assertThatThrownBy(() -> importarCandidatos.execute(new ImportarCandidatosRequest(2026, "nao-existe.zip")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("deve recusar arquivo fora do diretório de dados")
    void deveRecusarArquivoForaDoDiretorio() {
        assertThatThrownBy(() -> importarCandidatos.execute(new ImportarCandidatosRequest(2026, "../../etc/passwd")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diretório de dados");
        assertThatThrownBy(() -> importarCandidatos.execute(new ImportarCandidatosRequest(2026, "/etc/passwd")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(importador, never()).importarArquivo(any(), anyInt());
    }
}
