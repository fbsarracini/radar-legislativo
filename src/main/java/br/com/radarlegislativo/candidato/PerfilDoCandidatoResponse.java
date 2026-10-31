package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.ProposicaoResponse;

import java.util.List;

public record PerfilDoCandidatoResponse(CandidatoResponse candidato, List<VinculoResponse> vinculos,
                                        List<ProposicaoResponse> proposicoes) {

    public static PerfilDoCandidatoResponse of(PerfilDoCandidato perfil) {
        return new PerfilDoCandidatoResponse(
                CandidatoResponse.of(perfil.candidato()),
                perfil.vinculos().stream().map(VinculoResponse::of).toList(),
                perfil.proposicoes().stream().map(ProposicaoResponse::of).toList()
        );
    }
}
