package br.com.radarlegislativo.proposicao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColetaTest {

    private static final Instant AGORA = Instant.parse("2026-10-31T12:00:00Z");

    @Test
    @DisplayName("deve recusar janela invertida")
    void deveRecusarJanelaInvertida() {
        assertThatThrownBy(() -> new Coleta("camara", LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 30), AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("não deve terminar duas vezes")
    void naoDeveTerminarDuasVezes() {
        Coleta coleta = new Coleta("camara", LocalDate.of(2026, 10, 30), LocalDate.of(2026, 10, 31), AGORA);
        coleta.concluir(AGORA);

        assertThatThrownBy(() -> coleta.falhar("x", AGORA)).isInstanceOf(IllegalStateException.class);
    }
}
