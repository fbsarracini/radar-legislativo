package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.candidato.VinculoRepository;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
public class ConsultarProposicao {

    private static final Logger log = LoggerFactory.getLogger(ConsultarProposicao.class);

    private final ProposicaoColetadaRepository proposicaoRepository;
    private final VinculoRepository vinculoRepository;

    public ConsultarProposicao(ProposicaoColetadaRepository proposicaoRepository, VinculoRepository vinculoRepository) {
        this.proposicaoRepository = proposicaoRepository;
        this.vinculoRepository = vinculoRepository;
    }

    @Transactional(readOnly = true)
    public ProposicaoComCandidaturas execute(Long id) {
        AppLog.logger(log).system().does("consultar proposição").on("id=" + id).debug();
        ProposicaoColetada proposicao = proposicaoRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        proposicao.carregarAutores();
        return new ProposicaoComCandidaturas(proposicao, Candidaturas.dos(List.of(proposicao), vinculoRepository));
    }
}
