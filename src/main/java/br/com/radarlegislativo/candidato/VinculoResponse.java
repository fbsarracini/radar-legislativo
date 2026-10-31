package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.proposicao.ParlamentarColetadoResponse;

import java.time.Instant;

public record VinculoResponse(Long id, ParlamentarColetadoResponse parlamentar, Vinculo.Confianca confianca,
                              Vinculo.Status status, Instant decididoEm) {

    public static VinculoResponse of(Vinculo vinculo) {
        return new VinculoResponse(
                vinculo.id(),
                ParlamentarColetadoResponse.of(vinculo.parlamentar()),
                vinculo.confianca(),
                vinculo.status(),
                vinculo.decididoEm()
        );
    }
}
