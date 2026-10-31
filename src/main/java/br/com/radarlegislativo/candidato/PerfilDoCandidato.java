package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Proposicao;

import java.util.List;

/** Tudo de um candidato numa chamada — pensado para a tela de perfil. */
public record PerfilDoCandidato(Candidato candidato, List<Vinculo> vinculos, List<Proposicao> proposicoes) {
}
