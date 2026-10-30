package br.com.radarlegislativo.fonte;

import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class BuscarParlamentaresController {

    private final BuscarParlamentares buscarParlamentares;

    public BuscarParlamentaresController(BuscarParlamentares buscarParlamentares) {
        this.buscarParlamentares = buscarParlamentares;
    }

    @GetMapping("/api/parlamentares")
    @ResponseStatus(HttpStatus.OK)
    public List<ParlamentarResponse> execute(@RequestParam(required = false) String nome,
                                             @RequestParam(required = false) @Pattern(regexp = "[A-Za-z]{2}") String uf,
                                             @RequestParam(required = false) String fonte) {
        return buscarParlamentares.execute(nome, uf, fonte).stream()
                .map(ParlamentarResponse::of)
                .toList();
    }
}
