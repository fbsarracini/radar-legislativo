package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.FonteLegislativa;
import br.com.radarlegislativo.fonte.FonteRegistry;
import br.com.radarlegislativo.fonte.Nomes;
import br.com.radarlegislativo.log.AppLog;
import br.com.radarlegislativo.proposicao.ParlamentarColetado;
import br.com.radarlegislativo.proposicao.ParlamentarColetadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sugere vínculos entre parlamentares coletados e candidatos do TSE, só com o que está no banco.
 * Roda quando a coleta grava parlamentares novos e depois de cada importação do TSE.
 * Idempotente; sugestões que deixaram de casar somem, decisões manuais nunca são tocadas.
 */
@UseCase
public class VincularParlamentares {

    private static final Logger log = LoggerFactory.getLogger(VincularParlamentares.class);

    private final ParlamentarColetadoRepository parlamentarRepository;
    private final CandidatoRepository candidatoRepository;
    private final VinculoRepository vinculoRepository;
    private final FonteRegistry registry;
    private final Clock clock;

    public VincularParlamentares(ParlamentarColetadoRepository parlamentarRepository, CandidatoRepository candidatoRepository,
                                 VinculoRepository vinculoRepository, FonteRegistry registry, Clock clock) {
        this.parlamentarRepository = parlamentarRepository;
        this.candidatoRepository = candidatoRepository;
        this.vinculoRepository = vinculoRepository;
        this.registry = registry;
        this.clock = clock;
    }

    /** @return quantos vínculos (não rejeitados) os parlamentares têm depois do recálculo */
    @Transactional
    public int execute(Collection<Long> parlamentarIds) {
        return vincular(parlamentarRepository.findAllById(parlamentarIds));
    }

    @Transactional
    public int executeTodos() {
        return vincular(parlamentarRepository.findAll());
    }

    private int vincular(List<ParlamentarColetado> parlamentares) {
        // Muitos parlamentares dividem UF e cargo: cada grupo de candidatos é lido uma vez só.
        Map<String, List<Candidato>> candidatosPorGrupo = new HashMap<>();
        int total = 0;
        for (ParlamentarColetado p : parlamentares) {
            total += vincular(p, candidatosPorGrupo);
        }
        AppLog.logger(log).system().does("vincular parlamentares").on("parlamentares=" + parlamentares.size() + " vinculos=" + total).info();
        return total;
    }

    private int vincular(ParlamentarColetado p, Map<String, List<Candidato>> candidatosPorGrupo) {
        Optional<FonteLegislativa> fonte = registry.encontrar(p.fonte());
        if (fonte.isEmpty()) return 0;
        List<Candidato> candidatos = candidatosDoMandato(fonte.get(), p, candidatosPorGrupo);
        Map<Candidato, Vinculo.Confianca> sugestoes = new LinkedHashMap<>(Casamento.sugerir(p.nome(), candidatos));

        int total = 0;
        for (Vinculo existente : vinculoRepository.findByParlamentarId(p.id())) {
            Vinculo.Confianca conf = sugestoes.remove(existente.candidato());
            if (conf != null) {
                existente.recalcular(conf);
            } else if (existente.sugerido()) {
                vinculoRepository.delete(existente);
                continue;
            }
            if (existente.status() != Vinculo.Status.REJEITADO) total++;
        }
        sugestoes.forEach((c, conf) -> vinculoRepository.save(new Vinculo(c, p, conf, clock.instant())));
        return total + sugestoes.size();
    }

    private List<Candidato> candidatosDoMandato(FonteLegislativa fonte, ParlamentarColetado p,
                                                Map<String, List<Candidato>> cache) {
        Esfera esfera = fonte.esfera();
        // Casas federais têm parlamentares de todas as UFs; nas demais a UF é a da casa.
        boolean federal = esfera == Esfera.CAMARA_FEDERAL || esfera == Esfera.SENADO_FEDERAL;
        String uf = federal ? p.uf() : fonte.uf();
        if (uf == null) return List.of();
        List<String> cargos = Candidato.cargosDa(esfera);
        String municipio = esfera == Esfera.CAMARA_MUNICIPAL && fonte.municipio() != null ? Nomes.normalizar(fonte.municipio()) : null;
        return cache.computeIfAbsent(esfera + "|" + uf.toUpperCase() + "|" + municipio, k -> municipio == null
                ? candidatoRepository.findByUfAndCargoBuscaIn(uf.toUpperCase(), cargos)
                : candidatoRepository.findByUfAndCargoBuscaInAndMunicipioBusca(uf.toUpperCase(), cargos, municipio));
    }
}
