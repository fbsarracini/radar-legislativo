package br.com.radarlegislativo.eleito;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ImportarEleitosController {

    private final ImportarEleitos importarEleitos;

    public ImportarEleitosController(ImportarEleitos importarEleitos) {
        this.importarEleitos = importarEleitos;
    }

    @PostMapping("/api/admin/tse/importar")
    @ResponseStatus(HttpStatus.OK)
    public ImportarEleitosResponse execute(@RequestBody @Valid ImportarEleitosRequest request) {
        int total = importarEleitos.execute(request);
        return ImportarEleitosResponse.of(request.ano(), total);
    }
}
