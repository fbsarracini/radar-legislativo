package br.com.radarlegislativo.fonte;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.proposicao;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarProposicoesDoParlamentarTest {

    @Mock
    private FonteRegistry registry;

    @InjectMocks
    private ListarProposicoesDoParlamentar listarProposicoesDoParlamentar;

    @Test
    @DisplayName("deve ordenar da mais recente para a mais antiga, sem ano por último")
    void deveOrdenarDaMaisRecente() {
        FonteLegislativa camara = mock(FonteLegislativa.class);
        when(registry.porCodigo("camara")).thenReturn(camara);
        when(camara.proposicoesDoAutor("10", null)).thenReturn(List.of(
                proposicao("a", "PL", 1, 2023),
                proposicao("b", "PL", 9, null),
                proposicao("c", "PL", 5, 2025),
                proposicao("d", "PL", 7, 2025)));

        List<Proposicao> result = listarProposicoesDoParlamentar.execute("camara", "10", null);

        assertThat(result).extracting(Proposicao::id).containsExactly("d", "c", "a", "b");
    }
}
