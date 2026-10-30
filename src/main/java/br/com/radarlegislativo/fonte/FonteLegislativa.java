package br.com.radarlegislativo.fonte;

import java.util.List;

/**
 * Uma casa legislativa com API pública. Para adicionar uma nova (ex.: a API
 * própria de uma assembleia que não usa SAPL), basta implementar esta interface
 * e registrá-la no {@link FonteRegistry}.
 */
public interface FonteLegislativa {

    /** Identificador estável usado nas URLs da API (ex.: "camara", "senado", "sapl-campinas"). */
    String codigo();

    String nomeCasa();

    Esfera esfera();

    /** UF da casa; nulo para casas federais. */
    default String uf() {
        return null;
    }

    /** Município da casa; só para câmaras municipais. */
    default String municipio() {
        return null;
    }

    List<Parlamentar> buscarParlamentares(String nome, String uf);

    /** @param ano filtra pelo ano de apresentação; nulo = todos os anos que a fonte devolver */
    List<Proposicao> proposicoesDoAutor(String idParlamentar, Integer ano);
}
