package br.com.radarlegislativo.fonte;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class ListarProposicoesDoParlamentarController {

    private final ListarProposicoesDoParlamentar listarProposicoesDoParlamentar;

    public ListarProposicoesDoParlamentarController(ListarProposicoesDoParlamentar listarProposicoesDoParlamentar) {
        this.listarProposicoesDoParlamentar = listarProposicoesDoParlamentar;
    }

    @GetMapping("/api/fontes/{fonte}/parlamentares/{id}/proposicoes")
    @ResponseStatus(HttpStatus.OK)
    public List<ProposicaoResponse> execute(@PathVariable String fonte,
                                            @PathVariable String id,
                                            @RequestParam(required = false) @Positive Integer ano) {
        return listarProposicoesDoParlamentar.execute(fonte, id, ano).stream()
                .map(ProposicaoResponse::of)
                .toList();
    }
}
