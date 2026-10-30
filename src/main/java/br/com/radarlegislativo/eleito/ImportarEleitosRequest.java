package br.com.radarlegislativo.eleito;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ImportarEleitosRequest(
        @NotNull @Min(1994) Integer ano,
        String arquivo
) implements ImportacaoData {
}
