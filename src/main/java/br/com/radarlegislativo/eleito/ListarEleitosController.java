package br.com.radarlegislativo.eleito;

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
public class ListarEleitosController {

    private final ListarEleitos listarEleitos;

    public ListarEleitosController(ListarEleitos listarEleitos) {
        this.listarEleitos = listarEleitos;
    }

    @GetMapping("/api/eleitos")
    @ResponseStatus(HttpStatus.OK)
    public Page<EleitoResponse> execute(@RequestParam(required = false) @Positive Integer ano,
                                        @RequestParam(required = false) @Pattern(regexp = "[A-Za-z]{2}") String uf,
                                        @RequestParam(required = false) String cargo,
                                        @RequestParam(required = false) String nome,
                                        @RequestParam(required = false) String municipio,
                                        @PageableDefault(size = 20) Pageable pageable) {
        FiltroEleitos filtro = new FiltroEleitos(ano, uf, cargo, nome, municipio);
        return listarEleitos.execute(filtro, pageable)
                .map(EleitoResponse::of);
    }
}
