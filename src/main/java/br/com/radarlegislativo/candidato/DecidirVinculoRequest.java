package br.com.radarlegislativo.candidato;

import jakarta.validation.constraints.NotNull;

public record DecidirVinculoRequest(@NotNull Boolean confirmado) implements DecisaoDeVinculoData {
}
