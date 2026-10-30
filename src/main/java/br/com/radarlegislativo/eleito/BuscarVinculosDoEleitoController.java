package br.com.radarlegislativo.eleito;

import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class BuscarVinculosDoEleitoController {

    private final BuscarVinculosDoEleito buscarVinculosDoEleito;

    public BuscarVinculosDoEleitoController(BuscarVinculosDoEleito buscarVinculosDoEleito) {
        this.buscarVinculosDoEleito = buscarVinculosDoEleito;
    }

    @GetMapping("/api/eleitos/{sq}/vinculos")
    @ResponseStatus(HttpStatus.OK)
    public List<VinculoResponse> execute(@PathVariable @Pattern(regexp = "\\d+") String sq) {
        return buscarVinculosDoEleito.execute(sq).stream()
                .map(VinculoResponse::of)
                .toList();
    }
}
