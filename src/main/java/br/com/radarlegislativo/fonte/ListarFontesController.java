package br.com.radarlegislativo.fonte;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ListarFontesController {

    private final ListarFontes listarFontes;

    public ListarFontesController(ListarFontes listarFontes) {
        this.listarFontes = listarFontes;
    }

    @GetMapping("/api/fontes")
    @ResponseStatus(HttpStatus.OK)
    public List<FonteResponse> execute() {
        return listarFontes.execute().stream()
                .map(FonteResponse::of)
                .toList();
    }
}
