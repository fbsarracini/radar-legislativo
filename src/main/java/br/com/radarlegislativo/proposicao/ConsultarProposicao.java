package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class ConsultarProposicao {

    private static final Logger log = LoggerFactory.getLogger(ConsultarProposicao.class);

    private final ProposicaoColetadaRepository proposicaoRepository;

    public ConsultarProposicao(ProposicaoColetadaRepository proposicaoRepository) {
        this.proposicaoRepository = proposicaoRepository;
    }

    @Transactional(readOnly = true)
    public ProposicaoColetada execute(Long id) {
        AppLog.logger(log).system().does("consultar proposição").on("id=" + id).debug();
        ProposicaoColetada proposicao = proposicaoRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        proposicao.carregarAutores();
        return proposicao;
    }
}
