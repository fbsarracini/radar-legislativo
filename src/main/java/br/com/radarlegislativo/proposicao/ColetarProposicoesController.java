package br.com.radarlegislativo.proposicao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ColetarProposicoesController {

    private final ColetarProposicoes coletarProposicoes;

    public ColetarProposicoesController(ColetarProposicoes coletarProposicoes) {
        this.coletarProposicoes = coletarProposicoes;
    }

    /** Roda a coleta agora, sem esperar o agendamento; responde quando termina. */
    @PostMapping("/api/admin/coletas/{fonte}")
    @ResponseStatus(HttpStatus.OK)
    public ColetaResponse execute(@PathVariable String fonte) {
        return ColetaResponse.of(coletarProposicoes.execute(fonte));
    }
}
