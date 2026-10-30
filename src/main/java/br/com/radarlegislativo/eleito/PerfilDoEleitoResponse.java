package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.ProposicaoResponse;

import java.util.List;

public record PerfilDoEleitoResponse(EleitoResponse eleito, List<VinculoResponse> vinculos,
                                     List<ProposicaoResponse> proposicoes) {

    public static PerfilDoEleitoResponse of(PerfilDoEleito perfil) {
        return new PerfilDoEleitoResponse(
                EleitoResponse.of(perfil.eleito()),
                perfil.vinculos().stream().map(VinculoResponse::of).toList(),
                perfil.proposicoes().stream().map(ProposicaoResponse::of).toList()
        );
    }
}
