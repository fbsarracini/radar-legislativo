package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.log.AppLog;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/** Confirmação manual: resolve homônimos; o recálculo automático nunca desfaz a decisão. */
@UseCase
public class DecidirVinculo {

    private static final Logger log = LoggerFactory.getLogger(DecidirVinculo.class);

    private final VinculoRepository vinculoRepository;
    private final Clock clock;

    public DecidirVinculo(VinculoRepository vinculoRepository, Clock clock) {
        this.vinculoRepository = vinculoRepository;
        this.clock = clock;
    }

    @Transactional
    public Vinculo execute(String sq, Long idVinculo, @Valid DecisaoDeVinculoData dados) {
        Vinculo vinculo = vinculoRepository.findByIdAndCandidatoSqCandidato(idVinculo, sq)
                .orElseThrow(NotFoundException::new);
        vinculo.decidir(dados.confirmado(), clock.instant());
        AppLog.logger(log).system().does("decidir vínculo")
                .on("candidato=" + sq + " vinculo=" + idVinculo + " status=" + vinculo.status()).info();
        return vinculo;
    }
}
