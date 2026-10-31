package br.com.radarlegislativo.candidato;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class DecidirVinculoController {

    private final DecidirVinculo decidirVinculo;

    public DecidirVinculoController(DecidirVinculo decidirVinculo) {
        this.decidirVinculo = decidirVinculo;
    }

    @PutMapping("/api/candidatos/{sq}/vinculos/{id}/confirmacao")
    @ResponseStatus(HttpStatus.OK)
    public VinculoResponse execute(@PathVariable @Pattern(regexp = "\\d+") String sq,
                                   @PathVariable @Positive Long id,
                                   @RequestBody @Valid DecidirVinculoRequest request) {
        return VinculoResponse.of(decidirVinculo.execute(sq, id, request));
    }
}
