package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarProposicaoTest {

    @Mock
    private ProposicaoColetadaRepository proposicaoRepository;

    @InjectMocks
    private ConsultarProposicao consultarProposicao;

    @Test
    @DisplayName("deve lançar NotFound quando a proposição não existe")
    void deveLancarNotFound() {
        when(proposicaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultarProposicao.execute(99L)).isInstanceOf(NotFoundException.class);
    }
}
