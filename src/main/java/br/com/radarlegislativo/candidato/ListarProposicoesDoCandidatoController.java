package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.proposicao.FeedDeProposicoes;
import br.com.radarlegislativo.proposicao.ProposicaoColetadaResponse;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
public class ListarProposicoesDoCandidatoController {

    private final ListarProposicoesDoCandidato listarProposicoesDoCandidato;

    public ListarProposicoesDoCandidatoController(ListarProposicoesDoCandidato listarProposicoesDoCandidato) {
        this.listarProposicoesDoCandidato = listarProposicoesDoCandidato;
    }

    @GetMapping("/api/candidatos/{sq}/proposicoes")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProposicaoColetadaResponse> execute(
            @PathVariable @Pattern(regexp = "\\d+") String sq,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @PageableDefault(size = 20) Pageable pageable) {
        FeedDeProposicoes feed = listarProposicoesDoCandidato.execute(sq, desde, ate, pageable);
        return feed.pagina().map(p -> ProposicaoColetadaResponse.of(p, feed.candidaturas()));
    }
}
