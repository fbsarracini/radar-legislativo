package br.com.radarlegislativo.proposicao;

import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class ConsultarProposicaoController {

    private final ConsultarProposicao consultarProposicao;

    public ConsultarProposicaoController(ConsultarProposicao consultarProposicao) {
        this.consultarProposicao = consultarProposicao;
    }

    @GetMapping("/api/proposicoes/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposicaoColetadaResponse execute(@PathVariable @Positive Long id) {
        return ProposicaoColetadaResponse.of(consultarProposicao.execute(id));
    }
}
