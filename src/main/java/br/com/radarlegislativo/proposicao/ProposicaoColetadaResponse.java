package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.Esfera;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** @param id id interno, usado em /api/proposicoes/{id}; {@code idExterno} é o da casa */
public record ProposicaoColetadaResponse(Long id, String fonte, String idExterno, Esfera esfera, String casa,
                                         String sigla, Integer numero, Integer ano, String identificacao,
                                         String ementa, LocalDate dataApresentacao, String url,
                                         Instant primeiraVezVistaEm, List<AutoriaResponse> autores) {

    public static ProposicaoColetadaResponse of(ProposicaoColetada proposicao) {
        return new ProposicaoColetadaResponse(
                proposicao.id(),
                proposicao.fonte(),
                proposicao.idExterno(),
                proposicao.esfera(),
                proposicao.casa(),
                proposicao.sigla(),
                proposicao.numero(),
                proposicao.ano(),
                proposicao.identificacao(),
                proposicao.ementa(),
                proposicao.dataApresentacao(),
                proposicao.url(),
                proposicao.primeiraVezVistaEm(),
                proposicao.autores().stream().map(AutoriaResponse::of).toList()
        );
    }
}
