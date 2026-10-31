package br.com.radarlegislativo.candidato;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecalcularVinculosController {

    private final VincularParlamentares vincularParlamentares;

    public RecalcularVinculosController(VincularParlamentares vincularParlamentares) {
        this.vincularParlamentares = vincularParlamentares;
    }

    /** Refaz as sugestões de todos os parlamentares coletados (decisões manuais ficam). */
    @PostMapping("/api/admin/vinculos/recalcular")
    @ResponseStatus(HttpStatus.OK)
    public RecalcularVinculosResponse execute() {
        return new RecalcularVinculosResponse(vincularParlamentares.executeTodos());
    }

    /** @param vinculos vínculos não rejeitados depois do recálculo */
    public record RecalcularVinculosResponse(int vinculos) {}
}
