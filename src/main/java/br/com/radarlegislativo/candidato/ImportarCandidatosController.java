package br.com.radarlegislativo.candidato;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ImportarCandidatosController {

    private final ImportarCandidatos importarCandidatos;

    public ImportarCandidatosController(ImportarCandidatos importarCandidatos) {
        this.importarCandidatos = importarCandidatos;
    }

    @PostMapping("/api/admin/tse/importar")
    @ResponseStatus(HttpStatus.OK)
    public ImportarCandidatosResponse execute(@RequestBody @Valid ImportarCandidatosRequest request) {
        int total = importarCandidatos.execute(request);
        return ImportarCandidatosResponse.of(request.ano(), total);
    }
}
