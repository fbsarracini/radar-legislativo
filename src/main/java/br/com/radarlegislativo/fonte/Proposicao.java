package br.com.radarlegislativo.fonte;

import org.springframework.util.Assert;

import java.time.LocalDate;

/** Projeto / proposição protocolada, normalizada entre as diferentes fontes. */
public record Proposicao(
        String fonte,
        String id,
        Esfera esfera,
        String casa,
        String sigla,
        Integer numero,
        Integer ano,
        String ementa,
        LocalDate dataApresentacao,
        String situacao,
        String url) {

    public Proposicao {
        Assert.hasText(fonte, "a fonte é obrigatória");
        Assert.notNull(esfera, "a esfera é obrigatória");
    }

    /** Ex.: "PL 1234/2025". */
    public String identificacao() {
        if (sigla == null) {
            return null;
        }
        return sigla + (numero != null ? " " + numero : "") + (ano != null ? "/" + ano : "");
    }
}
