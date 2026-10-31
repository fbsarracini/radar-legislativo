package br.com.radarlegislativo.candidato;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ImportarCandidatosRequest(
        @NotNull @Min(1994) Integer ano,
        String arquivo
) implements ImportacaoData {
}
