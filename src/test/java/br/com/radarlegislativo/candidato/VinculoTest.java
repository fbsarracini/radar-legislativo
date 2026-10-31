package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.proposicao.ParlamentarColetado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static br.com.radarlegislativo.TestFixtures.deputado;
import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;

class VinculoTest {

    private static final Instant AGORA = Instant.parse("2026-10-31T12:00:00Z");

    private static Vinculo vinculo(String sq, Vinculo.Confianca confianca) {
        return new Vinculo(eleito(sq, 2026, "DEPUTADO FEDERAL", "SP", "X"),
                new ParlamentarColetado(deputado("1", "X"), AGORA), confianca, AGORA);
    }

    @Test
    @DisplayName("deve valer só o mais confiável entre sugestões, e só os confirmados quando houver")
    void deveEscolherVinculosValidos() {
        Vinculo exato = vinculo("1", Vinculo.Confianca.NOME_URNA_EXATO);
        Vinculo parcial = vinculo("2", Vinculo.Confianca.NOME_PARCIAL);
        Vinculo rejeitado = vinculo("3", Vinculo.Confianca.NOME_URNA_EXATO);
        rejeitado.decidir(false, AGORA);

        assertThat(Vinculo.validos(List.of(parcial, exato, rejeitado))).containsExactly(exato);

        parcial.decidir(true, AGORA);
        assertThat(Vinculo.validos(List.of(parcial, exato, rejeitado))).containsExactly(parcial);
        assertThat(Vinculo.validos(List.of(rejeitado))).isEmpty();
    }

    @Test
    @DisplayName("o recálculo não deve mexer em vínculo decidido")
    void recalculoNaoMexeEmDecidido() {
        Vinculo v = vinculo("1", Vinculo.Confianca.NOME_PARCIAL);
        v.recalcular(Vinculo.Confianca.NOME_URNA_EXATO);
        assertThat(v.confianca()).isEqualTo(Vinculo.Confianca.NOME_URNA_EXATO);

        v.decidir(true, AGORA);
        v.recalcular(Vinculo.Confianca.NOME_PARCIAL);
        assertThat(v.confianca()).isEqualTo(Vinculo.Confianca.NOME_URNA_EXATO);
        assertThat(v.status()).isEqualTo(Vinculo.Status.CONFIRMADO);
        assertThat(v.decididoEm()).isEqualTo(AGORA);
    }
}
