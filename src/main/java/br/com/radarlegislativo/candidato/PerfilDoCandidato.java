package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.proposicao.FeedDeProposicoes;

import java.util.List;

/** Tudo de um candidato numa chamada — pensado para a tela de perfil; os projetos são a primeira página. */
public record PerfilDoCandidato(Candidato candidato, List<Vinculo> vinculos, FeedDeProposicoes proposicoes) {
}
