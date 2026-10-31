package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.ProposicaoResponse;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class ListarProposicoesDoCandidatoController {

    private final ListarProposicoesDoCandidato listarProposicoesDoCandidato;

    public ListarProposicoesDoCandidatoController(ListarProposicoesDoCandidato listarProposicoesDoCandidato) {
        this.listarProposicoesDoCandidato = listarProposicoesDoCandidato;
    }

    @GetMapping("/api/candidatos/{sq}/proposicoes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProposicaoResponse> execute(@PathVariable @Pattern(regexp = "\\d+") String sq,
                                            @RequestParam(required = false) @Positive Integer ano) {
        return listarProposicoesDoCandidato.execute(sq, ano).stream()
                .map(ProposicaoResponse::of)
                .toList();
    }
}
