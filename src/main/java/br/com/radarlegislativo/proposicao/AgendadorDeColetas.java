package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.FonteComColeta;
import br.com.radarlegislativo.fonte.FonteRegistry;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dispara a coleta de todas as casas que a suportam; uma casa com problema não impede as outras. */
@Component
@ConditionalOnProperty(name = "radar.coleta.habilitada", havingValue = "true")
public class AgendadorDeColetas {

    private static final Logger log = LoggerFactory.getLogger(AgendadorDeColetas.class);

    private final FonteRegistry registry;
    private final ColetarProposicoes coletarProposicoes;

    public AgendadorDeColetas(FonteRegistry registry, ColetarProposicoes coletarProposicoes) {
        this.registry = registry;
        this.coletarProposicoes = coletarProposicoes;
    }

    @Scheduled(cron = "${radar.coleta.cron}")
    public void coletarTodas() {
        for (FonteComColeta fonte : registry.comColeta()) {
            try {
                coletarProposicoes.execute(fonte.codigo());
            } catch (RuntimeException e) {
                AppLog.logger(log).system().does("agendar coleta").on("fonte=" + fonte.codigo()).failed(e.getMessage()).error(e);
            }
        }
    }
}
