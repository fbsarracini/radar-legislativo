package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.config.CacheConfig;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.Comparator;
import java.util.List;

/** Projetos de autoria do parlamentar numa casa, do mais recente para o mais antigo. */
@UseCase
public class ListarProposicoesDoParlamentar {

    private static final Logger log = LoggerFactory.getLogger(ListarProposicoesDoParlamentar.class);

    private static final Comparator<Proposicao> MAIS_RECENTES_PRIMEIRO =
            Comparator.comparing(Proposicao::ano, Comparator.nullsLast(Comparator.<Integer>reverseOrder()))
                    .thenComparing(Proposicao::numero, Comparator.nullsLast(Comparator.reverseOrder()));

    private final FonteRegistry registry;

    public ListarProposicoesDoParlamentar(FonteRegistry registry) {
        this.registry = registry;
    }

    @Cacheable(cacheNames = CacheConfig.PROPOSICOES, key = "{#fonte, #idParlamentar, #ano}")
    public List<Proposicao> execute(String fonte, String idParlamentar, Integer ano) {
        String alvo = "fonte=" + fonte + " parlamentar=" + idParlamentar + " ano=" + ano;
        AppLog.logger(log).system().does("listar proposições do parlamentar").on(alvo).debug();
        List<Proposicao> result = registry.porCodigo(fonte).proposicoesDoAutor(idParlamentar, ano).stream()
                .sorted(MAIS_RECENTES_PRIMEIRO)
                .toList();
        AppLog.logger(log).system().does("listar proposições do parlamentar").on(alvo + " total=" + result.size()).info();
        return result;
    }
}
