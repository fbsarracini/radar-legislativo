package br.com.radarlegislativo.candidato;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarCandidatosTest {

    private static final FiltroCandidatos SEM_FILTRO = new FiltroCandidatos(null, null, null, null, null, null);

    @Mock
    private CandidatoRepository candidatoRepository;

    @InjectMocks
    private ListarCandidatos listarCandidatos;

    @Test
    @DisplayName("deve paginar no banco ordenando por nome de urna, ignorando a ordem pedida pelo cliente")
    void devePaginarNoBancoComOrdemFixa() {
        Candidato c = eleito("1", 2026, "SENADOR", "SP", "NOME");
        when(candidatoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(c), inv.getArgument(1), 25));

        Page<Candidato> result = listarCandidatos.execute(SEM_FILTRO, PageRequest.of(1, 10, Sort.by("situacao")));

        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(candidatoRepository).findAll(any(Specification.class), pagina.capture());
        assertThat(pagina.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pagina.getValue().getPageSize()).isEqualTo(10);
        assertThat(pagina.getValue().getSort()).isEqualTo(ListarCandidatos.POR_NOME_URNA);
        assertThat(result.getContent()).containsExactly(c);
        assertThat(result.getTotalElements()).isEqualTo(25);
    }
}
