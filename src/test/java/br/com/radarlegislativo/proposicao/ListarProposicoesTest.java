package br.com.radarlegislativo.proposicao;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarProposicoesTest {

    private static final FiltroProposicoes SEM_FILTRO =
            new FiltroProposicoes(null, null, null, null, null, null, null, null, null);

    @Mock
    private ProposicaoColetadaRepository proposicaoRepository;

    @InjectMocks
    private ListarProposicoes listarProposicoes;

    @Test
    @DisplayName("deve paginar no banco ignorando a ordem pedida pelo cliente (a ordem vem da Specification)")
    void devePaginarComOrdemFixa() {
        when(proposicaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<ProposicaoColetada>(List.of(), inv.getArgument(1), 0));

        Page<ProposicaoColetada> result = listarProposicoes.execute(SEM_FILTRO, PageRequest.of(2, 15, Sort.by("ementa")));

        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(proposicaoRepository).findAll(any(Specification.class), pagina.capture());
        assertThat(pagina.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pagina.getValue().getPageSize()).isEqualTo(15);
        assertThat(pagina.getValue().getSort().isUnsorted()).isTrue();
        assertThat(result).isEmpty();
    }
}
