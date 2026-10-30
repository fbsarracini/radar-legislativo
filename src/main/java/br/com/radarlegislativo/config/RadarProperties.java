package br.com.radarlegislativo.config;

import br.com.radarlegislativo.fonte.Esfera;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "radar")
public record RadarProperties(
        @NotNull @Valid Http http,
        @NotNull @Valid Cors cors,
        @NotNull @Valid Camara camara,
        @NotNull @Valid Senado senado,
        @NotNull List<@Valid CasaSapl> sapl,
        @NotNull @Valid Tse tse) {

    public record Http(@NotNull Duration connectTimeout, @NotNull Duration readTimeout, @NotBlank String userAgent) {}

    /** @param origens frontends liberados a chamar /api/** (React, Angular, Vue... em outra porta) */
    public record Cors(@NotEmpty List<String> origens) {}

    /**
     * @param legislaturas legislaturas consultadas ao buscar deputados (57 = 2023-2027, 58 = 2027-2031)
     * @param maxPaginas   limite de páginas de 100 itens por consulta de proposições
     */
    public record Camara(@NotBlank String baseUrl, @NotEmpty List<Integer> legislaturas, @Positive int maxPaginas) {}

    /**
     * @param parametroAutor nome do parâmetro de autoria do serviço /processo
     *                       (configurável porque o Senado reformulou a API em 2025)
     * @param legislaturas   legislaturas extras além de /senador/lista/atual
     */
    public record Senado(@NotBlank String baseUrl, @NotBlank String parametroAutor, @NotNull List<Integer> legislaturas) {}

    /**
     * Casa legislativa que usa o SAPL (Interlegis) — muitas câmaras municipais e
     * algumas assembleias estaduais. Cada instância expõe a mesma API REST.
     */
    public record CasaSapl(@NotBlank String slug, @NotBlank String nome, @NotNull Esfera esfera,
                           String uf, String municipio, @NotBlank String baseUrl) {}

    /**
     * @param urlTemplate    URL do zip consulta_cand; {ano} é substituído
     * @param diretorioCache onde guardar o zip baixado (é grande — centenas de MB); importações
     *                       de arquivo local só leem daqui
     */
    public record Tse(@NotBlank String urlTemplate, @NotBlank String diretorioCache) {}
}
