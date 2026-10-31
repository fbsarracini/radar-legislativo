package br.com.radarlegislativo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Radar Legislativo")
                        .description("Projetos protocolados nas casas legislativas, cruzados com os candidatos do TSE, a partir das APIs públicas (Câmara, Senado, SAPL) e dados do TSE")
                        .version("v1"));
    }
}
