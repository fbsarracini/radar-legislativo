package br.com.radarlegislativo.proposicao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/** Uma página do feed com as candidaturas dos autores já resolvidas. */
public record FeedDeProposicoes(Page<ProposicaoColetada> pagina, Candidaturas candidaturas) {

    public static FeedDeProposicoes vazio(Pageable pageable) {
        return new FeedDeProposicoes(Page.empty(pageable), new Candidaturas(Map.of()));
    }
}
