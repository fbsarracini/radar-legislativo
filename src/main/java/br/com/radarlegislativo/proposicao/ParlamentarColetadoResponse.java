package br.com.radarlegislativo.proposicao;

public record ParlamentarColetadoResponse(Long id, String fonte, String idExterno, String nome,
                                          String partido, String uf, String fotoUrl) {

    public static ParlamentarColetadoResponse of(ParlamentarColetado parlamentar) {
        return new ParlamentarColetadoResponse(
                parlamentar.id(),
                parlamentar.fonte(),
                parlamentar.idExterno(),
                parlamentar.nome(),
                parlamentar.partido(),
                parlamentar.uf(),
                parlamentar.fotoUrl()
        );
    }
}
