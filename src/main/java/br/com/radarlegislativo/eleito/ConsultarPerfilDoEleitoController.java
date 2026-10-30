package br.com.radarlegislativo.eleito;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
public class ConsultarPerfilDoEleitoController {

    private final ConsultarPerfilDoEleito consultarPerfilDoEleito;

    public ConsultarPerfilDoEleitoController(ConsultarPerfilDoEleito consultarPerfilDoEleito) {
        this.consultarPerfilDoEleito = consultarPerfilDoEleito;
    }

    @GetMapping("/api/eleitos/{sq}")
    @ResponseStatus(HttpStatus.OK)
    public PerfilDoEleitoResponse execute(@PathVariable @Pattern(regexp = "\\d+") String sq,
                                          @RequestParam(required = false) @Positive Integer ano) {
        return PerfilDoEleitoResponse.of(consultarPerfilDoEleito.execute(sq, ano));
    }
}
