package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.Proposicao;

import java.util.List;

/** Tudo de um eleito numa chamada — pensado para a tela de perfil. */
public record PerfilDoEleito(CandidatoEleito eleito, List<Vinculo> vinculos, List<Proposicao> proposicoes) {
}
