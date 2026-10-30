package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.Esfera;

public record EleitoResponse(String sqCandidato, int anoEleicao, String nome, String nomeUrna,
                             String cargo, Esfera esfera, String uf, String municipio,
                             String partido, String numero, String situacao) {

    public static EleitoResponse of(CandidatoEleito eleito) {
        return new EleitoResponse(
                eleito.sqCandidato(),
                eleito.anoEleicao(),
                eleito.nome(),
                eleito.nomeUrna(),
                eleito.cargo(),
                eleito.esferaDoMandato(),
                eleito.uf(),
                eleito.municipio(),
                eleito.partido(),
                eleito.numero(),
                eleito.situacao()
        );
    }
}
