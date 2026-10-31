package br.com.radarlegislativo.proposicao;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Validated
@RestController
public class ListarProposicoesController {

    private final ListarProposicoes listarProposicoes;

    public ListarProposicoesController(ListarProposicoes listarProposicoes) {
        this.listarProposicoes = listarProposicoes;
    }

    /**
     * @param tipo       uma ou mais siglas: tipo=PL&tipo=PEC ou tipo=PL,PEC
     * @param novasDesde ISO-8601 com fuso (ex.: 2026-10-30T18:00:00-03:00): o "novo desde a última visita"
     */
    @GetMapping("/api/proposicoes")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProposicaoColetadaResponse> execute(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime novasDesde,
            @RequestParam(required = false) String fonte,
            @RequestParam(required = false) List<String> tipo,
            @RequestParam(required = false) @Pattern(regexp = "[A-Za-z]{2}") String uf,
            @RequestParam(required = false) String partido,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) @Positive Long parlamentar,
            @PageableDefault(size = 20) Pageable pageable) {
        FiltroProposicoes filtro = new FiltroProposicoes(desde, ate, novasDesde == null ? null : novasDesde.toInstant(),
                fonte, tipo, uf, partido, autor, parlamentar);
        return listarProposicoes.execute(filtro, pageable)
                .map(ProposicaoColetadaResponse::of);
    }
}
