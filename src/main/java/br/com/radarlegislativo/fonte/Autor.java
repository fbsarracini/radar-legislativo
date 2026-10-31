package br.com.radarlegislativo.fonte;

/**
 * Autor de uma proposição, como a casa informa.
 *
 * @param idParlamentar id do parlamentar na própria casa; nulo quando o autor não é parlamentar
 *                      dela (Poder Executivo, comissão, Senado numa proposição da Câmara...)
 */
public record Autor(String nome, String tipo, String idParlamentar, boolean proponente) {
}
