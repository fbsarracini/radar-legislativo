package br.com.radarlegislativo.proposicao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ListarColetasController {

    private final ListarColetas listarColetas;

    public ListarColetasController(ListarColetas listarColetas) {
        this.listarColetas = listarColetas;
    }

    @GetMapping("/api/admin/coletas")
    @ResponseStatus(HttpStatus.OK)
    public List<ColetaResponse> execute() {
        return listarColetas.execute().stream()
                .map(ColetaResponse::of)
                .toList();
    }
}
