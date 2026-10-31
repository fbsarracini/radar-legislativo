package br.com.radarlegislativo.proposicao;

/** @param parlamentar nulo quando o autor não é parlamentar da casa (Executivo, comissão...) */
public record AutoriaResponse(int ordem, String nome, String tipo, boolean proponente,
                              ParlamentarColetadoResponse parlamentar) {

    public static AutoriaResponse of(Autoria autoria) {
        return new AutoriaResponse(
                autoria.ordem(),
                autoria.nome(),
                autoria.tipo(),
                autoria.proponente(),
                autoria.parlamentar() == null ? null : ParlamentarColetadoResponse.of(autoria.parlamentar())
        );
    }
}
