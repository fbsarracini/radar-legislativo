package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.ParlamentarResponse;

public record VinculoResponse(ParlamentarResponse parlamentar, Vinculo.Confianca confianca) {

    public static VinculoResponse of(Vinculo vinculo) {
        return new VinculoResponse(ParlamentarResponse.of(vinculo.parlamentar()), vinculo.confianca());
    }
}
