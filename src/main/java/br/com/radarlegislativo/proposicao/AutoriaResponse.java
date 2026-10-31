package br.com.radarlegislativo.proposicao;

import java.util.List;

/**
 * @param parlamentar  nulo quando o autor não é parlamentar da casa (Executivo, comissão...)
 * @param candidaturas candidaturas do autor no TSE (sugeridas ou confirmadas) — o selo do feed
 */
public record AutoriaResponse(int ordem, String nome, String tipo, boolean proponente,
                              ParlamentarColetadoResponse parlamentar, List<CandidaturaResponse> candidaturas) {

    public static AutoriaResponse of(Autoria autoria, Candidaturas candidaturas) {
        return new AutoriaResponse(
                autoria.ordem(),
                autoria.nome(),
                autoria.tipo(),
                autoria.proponente(),
                autoria.parlamentar() == null ? null : ParlamentarColetadoResponse.of(autoria.parlamentar()),
                candidaturas.de(autoria.parlamentar()).stream().map(CandidaturaResponse::of).toList()
        );
    }
}
