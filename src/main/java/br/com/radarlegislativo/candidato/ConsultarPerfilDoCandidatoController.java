package br.com.radarlegislativo.candidato;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
public class ConsultarPerfilDoCandidatoController {

    private final ConsultarPerfilDoCandidato consultarPerfilDoCandidato;

    public ConsultarPerfilDoCandidatoController(ConsultarPerfilDoCandidato consultarPerfilDoCandidato) {
        this.consultarPerfilDoCandidato = consultarPerfilDoCandidato;
    }

    @GetMapping("/api/candidatos/{sq}")
    @ResponseStatus(HttpStatus.OK)
    public PerfilDoCandidatoResponse execute(@PathVariable @Pattern(regexp = "\\d+") String sq,
                                          @RequestParam(required = false) @Positive Integer ano) {
        return PerfilDoCandidatoResponse.of(consultarPerfilDoCandidato.execute(sq, ano));
    }
}
