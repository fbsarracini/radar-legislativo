package br.com.radarlegislativo.fonte;

import org.springframework.util.Assert;

/**
 * Parlamentar como cadastrado na casa legislativa (o id é o da própria casa:
 * id do deputado na Câmara, código do senador no Senado, id do autor no SAPL).
 */
public record Parlamentar(
        String fonte,
        String id,
        Esfera esfera,
        String casa,
        String nome,
        String partido,
        String uf,
        String fotoUrl) {

    public Parlamentar {
        Assert.hasText(fonte, "a fonte é obrigatória");
        Assert.hasText(id, "o id é obrigatório");
        Assert.notNull(esfera, "a esfera é obrigatória");
    }
}
