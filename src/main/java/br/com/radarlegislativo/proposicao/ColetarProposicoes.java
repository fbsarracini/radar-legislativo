package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.config.RadarProperties;
import br.com.radarlegislativo.exception.FonteIndisponivelException;
import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.FonteComColeta;
import br.com.radarlegislativo.fonte.FonteRegistry;
import br.com.radarlegislativo.fonte.Proposicao;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Traz para o banco o que foi protocolado numa casa desde a última coleta bem-sucedida.
 * <ul>
 *   <li>Idempotente: rodar de novo a mesma janela só atualiza o que já existe.</li>
 *   <li>Autores e parlamentares só são pedidos à casa para o que ainda não está no banco.</li>
 *   <li>Qualquer falha da casa encerra a coleta como FALHA; a próxima recomeça da última com SUCESSO,
 *       então nenhum dia fica para trás.</li>
 * </ul>
 * O lock é por instância da app; com mais de uma rodando, troque por um lock no banco.
 */
@UseCase
public class ColetarProposicoes {

    private static final Logger log = LoggerFactory.getLogger(ColetarProposicoes.class);

    private final FonteRegistry registry;
    private final ColetaRepository coletaRepository;
    private final GravadorDeProposicoes gravador;
    private final int diasIniciais;
    private final Clock clock;
    private final Map<String, ReentrantLock> emAndamento = new ConcurrentHashMap<>();

    public ColetarProposicoes(FonteRegistry registry, ColetaRepository coletaRepository,
                              GravadorDeProposicoes gravador, RadarProperties props, Clock clock) {
        this.registry = registry;
        this.coletaRepository = coletaRepository;
        this.gravador = gravador;
        this.diasIniciais = props.coleta().diasIniciais();
        this.clock = clock;
    }

    public Coleta execute(String codigoFonte) {
        FonteComColeta fonte = registry.comColeta(codigoFonte);
        ReentrantLock lock = emAndamento.computeIfAbsent(fonte.codigo(), c -> new ReentrantLock());
        Assert.isTrue(lock.tryLock(), "já existe uma coleta em andamento para " + fonte.codigo());
        try {
            return coletar(fonte);
        } finally {
            lock.unlock();
        }
    }

    private Coleta coletar(FonteComColeta fonte) {
        LocalDate ate = LocalDate.now(clock);
        Coleta coleta = coletaRepository.save(new Coleta(fonte.codigo(), inicioDaJanela(fonte.codigo(), ate), ate, clock.instant()));
        String alvo = "fonte=" + fonte.codigo() + " de=" + coleta.de() + " ate=" + coleta.ate();
        AppLog.logger(log).system().does("coletar proposições").on(alvo).debug();
        try {
            for (Proposicao p : fonte.proposicoesApresentadasEntre(coleta.de(), coleta.ate())) {
                if (gravador.atualizarSeExistir(p, clock.instant())) {
                    coleta.contarExistente();
                    continue;
                }
                List<Autor> autores = fonte.autoresDa(p.id());
                for (String id : gravador.parlamentaresDesconhecidos(fonte.codigo(), idsDosParlamentares(autores))) {
                    gravador.registrarParlamentar(fonte.parlamentar(id), clock.instant());
                }
                gravador.criar(p, autores, clock.instant());
                coleta.contarNova();
            }
            coleta.concluir(clock.instant());
            AppLog.logger(log).system().does("coletar proposições")
                    .on(alvo + " novas=" + coleta.novas() + " existentes=" + coleta.existentes()).info();
        } catch (FonteIndisponivelException e) {
            coleta.falhar(e.getMessage(), clock.instant());
            AppLog.logger(log).system().does("coletar proposições").on(alvo + " novas=" + coleta.novas()).failed(e.getMessage()).warn();
        } catch (RuntimeException e) {
            coleta.falhar(e.getClass().getSimpleName(), clock.instant());
            coletaRepository.save(coleta);
            throw e;
        }
        return coletaRepository.save(coleta);
    }

    /**
     * Um dia de sobreposição com a última coleta bem-sucedida: a Câmara filtra por dia,
     * e o que foi protocolado depois da última execução daquele dia ainda precisa entrar.
     */
    private LocalDate inicioDaJanela(String fonte, LocalDate hoje) {
        return coletaRepository.findFirstByFonteAndStatusOrderByAteDesc(fonte, Coleta.Status.SUCESSO)
                .map(ultima -> ultima.ate().minusDays(1))
                .orElse(hoje.minusDays(diasIniciais));
    }

    static List<String> idsDosParlamentares(List<Autor> autores) {
        return autores.stream().map(Autor::idParlamentar).filter(Objects::nonNull).distinct().toList();
    }
}
