package br.com.radarlegislativo.fonte;

import java.time.LocalDate;

public record ProposicaoResponse(String fonte, String id, Esfera esfera, String casa,
                                 String sigla, Integer numero, Integer ano, String identificacao,
                                 String ementa, LocalDate dataApresentacao, String situacao, String url) {

    public static ProposicaoResponse of(Proposicao proposicao) {
        return new ProposicaoResponse(
                proposicao.fonte(),
                proposicao.id(),
                proposicao.esfera(),
                proposicao.casa(),
                proposicao.sigla(),
                proposicao.numero(),
                proposicao.ano(),
                proposicao.identificacao(),
                proposicao.ementa(),
                proposicao.dataApresentacao(),
                proposicao.situacao(),
                proposicao.url()
        );
    }
}
