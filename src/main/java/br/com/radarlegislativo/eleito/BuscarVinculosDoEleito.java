package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.FonteIndisponivelException;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.fonte.BuscarParlamentares;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.FonteLegislativa;
import br.com.radarlegislativo.fonte.FonteRegistry;
import br.com.radarlegislativo.fonte.Nomes;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Procura o eleito nas casas onde ele exerce (ou exercerá) o mandato.
 * Pode haver mais de um vínculo — ex.: deputado reeleito aparece na legislatura atual e na próxima
 * com o mesmo id, ou homônimos; por isso devolvemos a confiança de cada um.
 */
@UseCase
public class BuscarVinculosDoEleito {

    private static final Logger log = LoggerFactory.getLogger(BuscarVinculosDoEleito.class);

    private final EleitoRepository eleitoRepository;
    private final FonteRegistry registry;
    private final BuscarParlamentares buscarParlamentares;

    public BuscarVinculosDoEleito(EleitoRepository eleitoRepository, FonteRegistry registry,
                                  BuscarParlamentares buscarParlamentares) {
        this.eleitoRepository = eleitoRepository;
        this.registry = registry;
        this.buscarParlamentares = buscarParlamentares;
    }

    public List<Vinculo> execute(String sq) {
        AppLog.logger(log).system().does("buscar vínculos").on("eleito=" + sq).debug();
        CandidatoEleito eleito = eleitoRepository.buscarPorSq(sq)
                .orElseThrow(NotFoundException::new);

        Esfera esfera = eleito.esferaDoMandato();
        if (esfera == null) {
            AppLog.logger(log).system().does("buscar vínculos").on("eleito=" + sq + " cargo=" + eleito.cargo()).info();
            return List.of();
        }

        List<Vinculo> result = new ArrayList<>();
        for (FonteLegislativa fonte : registry.paraMandato(esfera, eleito.uf(), eleito.municipio())) {
            List<Parlamentar> candidatos;
            try {
                candidatos = buscarParlamentares.execute(termoDeBusca(eleito), ufDaBusca(esfera, eleito), fonte.codigo());
            } catch (FonteIndisponivelException e) {
                AppLog.logger(log).system().does("buscar vínculos").on("eleito=" + sq + " fonte=" + fonte.codigo()).failed(e.getMessage()).warn();
                continue;
            }
            for (Parlamentar p : candidatos) {
                classificar(eleito, p).ifPresent(c -> result.add(new Vinculo(eleito, p, c)));
            }
        }
        result.sort(Comparator.comparing(Vinculo::confianca));
        AppLog.logger(log).system().does("buscar vínculos").on("eleito=" + sq + " total=" + result.size()).info();
        return result;
    }

    static Optional<Vinculo.Confianca> classificar(CandidatoEleito e, Parlamentar p) {
        String nomeCasa = Nomes.chave(p.nome());
        if (!nomeCasa.isEmpty() && nomeCasa.equals(Nomes.chave(e.nomeUrna()))) {
            return Optional.of(Vinculo.Confianca.NOME_URNA_EXATO);
        }
        if (!nomeCasa.isEmpty() && nomeCasa.equals(Nomes.chave(e.nome()))) {
            return Optional.of(Vinculo.Confianca.NOME_CIVIL_EXATO);
        }
        if (Nomes.contidoEm(p.nome(), e.nome()) || Nomes.contidoEm(e.nomeUrna(), p.nome())) {
            return Optional.of(Vinculo.Confianca.NOME_PARCIAL);
        }
        return Optional.empty();
    }

    /** A palavra mais longa do nome de urna (no empate, a última — o sobrenome) é um bom filtro para a API. */
    static String termoDeBusca(CandidatoEleito e) {
        String base = Nomes.chave(e.nomeUrna() != null ? e.nomeUrna() : e.nome());
        String melhor = base;
        int tamanho = -1;
        for (String parte : base.split(" ")) {
            if (parte.length() >= tamanho) {
                melhor = parte;
                tamanho = parte.length();
            }
        }
        return melhor;
    }

    private static String ufDaBusca(Esfera esfera, CandidatoEleito e) {
        return (esfera == Esfera.CAMARA_FEDERAL || esfera == Esfera.SENADO_FEDERAL) ? e.uf() : null;
    }
}
