package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.config.CacheConfig;
import br.com.radarlegislativo.exception.FonteIndisponivelException;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

/**
 * Busca em uma fonte específica ou, sem {@code fonte}, em todas.
 * Uma fonte fora do ar não derruba a busca nas outras.
 */
@UseCase
public class BuscarParlamentares {

    private static final Logger log = LoggerFactory.getLogger(BuscarParlamentares.class);

    private final FonteRegistry registry;

    public BuscarParlamentares(FonteRegistry registry) {
        this.registry = registry;
    }

    @Cacheable(cacheNames = CacheConfig.PARLAMENTARES, key = "{#nome, #uf, #fonte}")
    public List<Parlamentar> execute(String nome, String uf, String fonte) {
        String filtro = "nome=" + nome + " uf=" + uf + " fonte=" + fonte;
        AppLog.logger(log).system().does("buscar parlamentares").on(filtro).debug();

        if (fonte != null && !fonte.isBlank()) {
            List<Parlamentar> result = registry.porCodigo(fonte).buscarParlamentares(nome, uf);
            AppLog.logger(log).system().does("buscar parlamentares").on(filtro + " total=" + result.size()).info();
            return result;
        }

        List<Parlamentar> result = new ArrayList<>();
        for (FonteLegislativa f : registry.todas()) {
            try {
                result.addAll(f.buscarParlamentares(nome, uf));
            } catch (FonteIndisponivelException e) {
                AppLog.logger(log).system().does("buscar parlamentares").on("fonte=" + f.codigo()).failed(e.getMessage()).warn();
            }
        }
        AppLog.logger(log).system().does("buscar parlamentares").on(filtro + " total=" + result.size()).info();
        return result;
    }
}
