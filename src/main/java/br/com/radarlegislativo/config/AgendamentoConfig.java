package br.com.radarlegislativo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AgendamentoConfig {

    /**
     * Relógio injetável: a janela da coleta depende de "hoje", e os testes precisam fixá-lo.
     * Fuso de Brasília porque as casas filtram pela data local de apresentação, não importa o fuso do servidor.
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "radar.coleta.habilitada", havingValue = "true")
    static class Agendamento {
    }
}
