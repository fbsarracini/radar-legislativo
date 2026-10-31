package br.com.radarlegislativo.proposicao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiltroProposicoesTest {

    @Test
    @DisplayName("deve recusar 'desde' depois de 'ate'")
    void deveRecusarPeriodoInvertido() {
        assertThatThrownBy(() -> new FiltroProposicoes(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1),
                null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desde");
    }

    @Test
    @DisplayName("deve normalizar as siglas e descartar vazias")
    void deveNormalizarTipos() {
        FiltroProposicoes filtro = new FiltroProposicoes(null, null, null, null, List.of(" pl", "", "PEC"),
                null, null, null, null);

        assertThat(filtro.tipos()).containsExactly("PL", "PEC");
        assertThat(new FiltroProposicoes(null, null, null, null, null, null, null, null, null).tipos()).isEmpty();
    }
}
