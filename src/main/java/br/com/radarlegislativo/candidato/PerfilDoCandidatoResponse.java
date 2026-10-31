package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.proposicao.ProposicaoColetadaResponse;

import java.util.List;

/** @param totalProposicoes total de projetos; os demais vêm de /api/candidatos/{sq}/proposicoes */
public record PerfilDoCandidatoResponse(CandidatoResponse candidato, List<VinculoResponse> vinculos,
                                        List<ProposicaoColetadaResponse> proposicoes, long totalProposicoes) {

    public static PerfilDoCandidatoResponse of(PerfilDoCandidato perfil) {
        return new PerfilDoCandidatoResponse(
                CandidatoResponse.of(perfil.candidato()),
                perfil.vinculos().stream().map(VinculoResponse::of).toList(),
                perfil.proposicoes().pagina().getContent().stream()
                        .map(p -> ProposicaoColetadaResponse.of(p, perfil.proposicoes().candidaturas()))
                        .toList(),
                perfil.proposicoes().pagina().getTotalElements()
        );
    }
}
