package br.com.radarlegislativo.fonte;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NomesTest {

    @Test
    @DisplayName("deve remover acentos, pontuação e espaços extras")
    void deveNormalizar() {
        assertThat(Nomes.normalizar("  José   da Conceição! ")).isEqualTo("JOSE DA CONCEICAO");
    }

    @Test
    @DisplayName("deve tratar nulo como vazio")
    void deveTratarNuloComoVazio() {
        assertThat(Nomes.normalizar(null)).isEmpty();
        assertThat(Nomes.chave(null)).isEmpty();
    }

    @Test
    @DisplayName("deve remover títulos e partículas na chave")
    void deveRemoverTitulosEParticulas() {
        assertThat(Nomes.chave("Dr. José da Conceição")).isEqualTo("JOSE CONCEICAO");
    }

    @Test
    @DisplayName("deve reconhecer nome curto contido no longo")
    void deveReconhecerNomeContido() {
        assertThat(Nomes.contidoEm("João Silva", "JOAO PEREIRA DA SILVA")).isTrue();
        assertThat(Nomes.contidoEm("João Souza", "JOAO PEREIRA DA SILVA")).isFalse();
        assertThat(Nomes.contidoEm("", "JOAO")).isFalse();
    }
}
