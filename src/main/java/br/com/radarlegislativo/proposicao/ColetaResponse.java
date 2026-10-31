package br.com.radarlegislativo.proposicao;

import java.time.Instant;
import java.time.LocalDate;

public record ColetaResponse(Long id, String fonte, LocalDate de, LocalDate ate, Coleta.Status status,
                             int novas, int existentes, String erro, Instant iniciadaEm, Instant terminadaEm) {

    public static ColetaResponse of(Coleta coleta) {
        return new ColetaResponse(
                coleta.id(),
                coleta.fonte(),
                coleta.de(),
                coleta.ate(),
                coleta.status(),
                coleta.novas(),
                coleta.existentes(),
                coleta.erro(),
                coleta.iniciadaEm(),
                coleta.terminadaEm()
        );
    }
}
