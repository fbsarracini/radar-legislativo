package br.com.radarlegislativo.fonte;

public record ParlamentarResponse(String fonte, String id, Esfera esfera, String casa,
                                  String nome, String partido, String uf, String fotoUrl) {

    public static ParlamentarResponse of(Parlamentar parlamentar) {
        return new ParlamentarResponse(
                parlamentar.fonte(),
                parlamentar.id(),
                parlamentar.esfera(),
                parlamentar.casa(),
                parlamentar.nome(),
                parlamentar.partido(),
                parlamentar.uf(),
                parlamentar.fotoUrl()
        );
    }
}
