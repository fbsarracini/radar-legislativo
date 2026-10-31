package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.log.AppLog;
import br.com.radarlegislativo.proposicao.FeedDeProposicoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
public class ConsultarPerfilDoCandidato {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPerfilDoCandidato.class);

    static final int PROPOSICOES_NO_PERFIL = 20;

    private final CandidatoRepository candidatoRepository;
    private final BuscarVinculosDoCandidato buscarVinculosDoCandidato;
    private final ListarProposicoesDoCandidato listarProposicoesDoCandidato;

    public ConsultarPerfilDoCandidato(CandidatoRepository candidatoRepository,
                                      BuscarVinculosDoCandidato buscarVinculosDoCandidato,
                                      ListarProposicoesDoCandidato listarProposicoesDoCandidato) {
        this.candidatoRepository = candidatoRepository;
        this.buscarVinculosDoCandidato = buscarVinculosDoCandidato;
        this.listarProposicoesDoCandidato = listarProposicoesDoCandidato;
    }

    /** Uma transação só: vínculos e projetos saem carregados para a resposta. */
    @Transactional(readOnly = true)
    public PerfilDoCandidato execute(String sq) {
        AppLog.logger(log).system().does("consultar perfil").on("candidato=" + sq).debug();
        Candidato candidato = candidatoRepository.findById(sq)
                .orElseThrow(NotFoundException::new);
        List<Vinculo> vinculos = buscarVinculosDoCandidato.execute(sq);
        FeedDeProposicoes proposicoes = listarProposicoesDoCandidato.execute(vinculos, null, null,
                PageRequest.of(0, PROPOSICOES_NO_PERFIL));
        AppLog.logger(log).system().does("consultar perfil").on("candidato=" + sq).info();
        return new PerfilDoCandidato(candidato, vinculos, proposicoes);
    }
}
