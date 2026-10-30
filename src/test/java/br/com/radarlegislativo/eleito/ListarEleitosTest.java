package br.com.radarlegislativo.eleito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.IntStream;

import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarEleitosTest {

    private static final FiltroEleitos SEM_FILTRO = new FiltroEleitos(null, null, null, null, null);

    @Mock
    private EleitoRepository eleitoRepository;

    @InjectMocks
    private ListarEleitos listarEleitos;

    private static List<CandidatoEleito> eleitos(int quantidade) {
        return IntStream.rangeClosed(1, quantidade)
                .mapToObj(i -> eleito(String.valueOf(i), 2026, "SENADOR", "SP", "NOME " + i))
                .toList();
    }

    @Test
    @DisplayName("deve devolver a página pedida com o total")
    void deveDevolverPaginaPedida() {
        when(eleitoRepository.buscar(SEM_FILTRO)).thenReturn(eleitos(25));

        Page<CandidatoEleito> result = listarEleitos.execute(SEM_FILTRO, PageRequest.of(1, 10));

        assertThat(result.getContent()).extracting(CandidatoEleito::sqCandidato).first().isEqualTo("11");
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
    }

    @Test
    @DisplayName("deve devolver página vazia quando passa do fim")
    void deveDevolverPaginaVaziaAlemDoFim() {
        when(eleitoRepository.buscar(SEM_FILTRO)).thenReturn(eleitos(5));

        Page<CandidatoEleito> result = listarEleitos.execute(SEM_FILTRO, PageRequest.of(3, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(5);
    }
}
