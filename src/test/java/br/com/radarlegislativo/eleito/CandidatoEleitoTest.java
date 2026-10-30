package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.Esfera;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CandidatoEleitoTest {

    @Test
    @DisplayName("deve lançar exceção quando sq é vazio")
    void deveLancarQuandoSqVazio() {
        assertThatThrownBy(() -> eleito(" ", 2026, "SENADOR", "RJ", "X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sq");
    }

    @Test
    @DisplayName("deve lançar exceção quando ano não é positivo")
    void deveLancarQuandoAnoInvalido() {
        assertThatThrownBy(() -> eleito("1", 0, "SENADOR", "RJ", "X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ano");
    }

    @Test
    @DisplayName("deve mapear o cargo para a esfera do mandato")
    void deveMapearCargoParaEsfera() {
        assertThat(eleito("1", 2026, "DEPUTADO FEDERAL", "SP", "X").esferaDoMandato()).isEqualTo(Esfera.CAMARA_FEDERAL);
        assertThat(eleito("1", 2026, "SENADOR", "SP", "X").esferaDoMandato()).isEqualTo(Esfera.SENADO_FEDERAL);
        assertThat(eleito("1", 2026, "DEPUTADO DISTRITAL", "DF", "X").esferaDoMandato()).isEqualTo(Esfera.ASSEMBLEIA_ESTADUAL);
        assertThat(eleito("1", 2024, "VEREADOR", "SP", "X").esferaDoMandato()).isEqualTo(Esfera.CAMARA_MUNICIPAL);
    }

    @Test
    @DisplayName("deve retornar esfera nula para cargo executivo")
    void deveRetornarNuloParaExecutivo() {
        assertThat(eleito("1", 2026, "GOVERNADOR", "MG", "X").esferaDoMandato()).isNull();
    }
}
