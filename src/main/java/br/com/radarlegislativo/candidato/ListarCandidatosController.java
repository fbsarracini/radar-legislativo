package br.com.radarlegislativo.candidato;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
public class ListarCandidatosController {

    private final ListarCandidatos listarCandidatos;

    public ListarCandidatosController(ListarCandidatos listarCandidatos) {
        this.listarCandidatos = listarCandidatos;
    }

    @GetMapping("/api/candidatos")
    @ResponseStatus(HttpStatus.OK)
    public Page<CandidatoResponse> execute(@RequestParam(required = false) @Positive Integer ano,
                                           @RequestParam(required = false) @Pattern(regexp = "[A-Za-z]{2}") String uf,
                                           @RequestParam(required = false) String cargo,
                                           @RequestParam(required = false) String nome,
                                           @RequestParam(required = false) String municipio,
                                           @RequestParam(required = false) Boolean eleito,
                                           @PageableDefault(size = 20) Pageable pageable) {
        FiltroCandidatos filtro = new FiltroCandidatos(ano, uf, cargo, nome, municipio, eleito);
        return listarCandidatos.execute(filtro, pageable)
                .map(CandidatoResponse::of);
    }
}
