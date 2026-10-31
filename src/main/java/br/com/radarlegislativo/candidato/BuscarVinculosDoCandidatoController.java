package br.com.radarlegislativo.candidato;

import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
public class BuscarVinculosDoCandidatoController {

    private final BuscarVinculosDoCandidato buscarVinculosDoCandidato;

    public BuscarVinculosDoCandidatoController(BuscarVinculosDoCandidato buscarVinculosDoCandidato) {
        this.buscarVinculosDoCandidato = buscarVinculosDoCandidato;
    }

    @GetMapping("/api/candidatos/{sq}/vinculos")
    @ResponseStatus(HttpStatus.OK)
    public List<VinculoResponse> execute(@PathVariable @Pattern(regexp = "\\d+") String sq) {
        return buscarVinculosDoCandidato.execute(sq).stream()
                .map(VinculoResponse::of)
                .toList();
    }
}
