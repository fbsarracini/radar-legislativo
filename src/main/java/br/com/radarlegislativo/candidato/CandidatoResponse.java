package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Esfera;

public record CandidatoResponse(String sqCandidato, int anoEleicao, String nome, String nomeUrna,
                                String cargo, Esfera esfera, String uf, String municipio,
                                String partido, String numero, String situacao, boolean eleito) {

    public static CandidatoResponse of(Candidato candidato) {
        return new CandidatoResponse(
                candidato.sqCandidato(),
                candidato.anoEleicao(),
                candidato.nome(),
                candidato.nomeUrna(),
                candidato.cargo(),
                candidato.esferaDoMandato(),
                candidato.uf(),
                candidato.municipio(),
                candidato.partido(),
                candidato.numero(),
                candidato.situacao(),
                candidato.eleito()
        );
    }
}
