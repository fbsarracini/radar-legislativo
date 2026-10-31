package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Vínculos gravados do candidato, do mais para o menos confiável (rejeitados inclusive, para a tela de revisão).
 * Pode haver mais de um — ex.: homônimos, ou o mesmo nome em duas casas.
 */
@UseCase
public class BuscarVinculosDoCandidato {

    private static final Logger log = LoggerFactory.getLogger(BuscarVinculosDoCandidato.class);

    private static final Comparator<Vinculo> MAIS_CONFIAVEIS_PRIMEIRO =
            Comparator.comparing(Vinculo::confianca).thenComparing(Vinculo::id);

    private final CandidatoRepository candidatoRepository;
    private final VinculoRepository vinculoRepository;

    public BuscarVinculosDoCandidato(CandidatoRepository candidatoRepository, VinculoRepository vinculoRepository) {
        this.candidatoRepository = candidatoRepository;
        this.vinculoRepository = vinculoRepository;
    }

    @Transactional(readOnly = true)
    public List<Vinculo> execute(String sq) {
        AppLog.logger(log).system().does("buscar vínculos").on("candidato=" + sq).debug();
        if (!candidatoRepository.existsById(sq)) throw new NotFoundException();
        List<Vinculo> result = vinculoRepository.findByCandidatoSqCandidato(sq).stream()
                .sorted(MAIS_CONFIAVEIS_PRIMEIRO)
                .toList();
        AppLog.logger(log).system().does("buscar vínculos").on("candidato=" + sq + " total=" + result.size()).info();
        return result;
    }
}
