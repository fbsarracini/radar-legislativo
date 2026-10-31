package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.candidato.Candidato;
import br.com.radarlegislativo.candidato.Vinculo;

/** Candidatura do autor no TSE, vista pelo vínculo; {@code status} SUGERIDO ainda pode ser homônimo. */
public record CandidaturaResponse(Long vinculo, String sqCandidato, int anoEleicao, String cargo, String uf,
                                  String partido, String situacao, boolean eleito,
                                  Vinculo.Confianca confianca, Vinculo.Status status) {

    public static CandidaturaResponse of(Vinculo vinculo) {
        Candidato c = vinculo.candidato();
        return new CandidaturaResponse(
                vinculo.id(),
                c.sqCandidato(),
                c.anoEleicao(),
                c.cargo(),
                c.uf(),
                c.partido(),
                c.situacao(),
                c.eleito(),
                vinculo.confianca(),
                vinculo.status()
        );
    }
}
