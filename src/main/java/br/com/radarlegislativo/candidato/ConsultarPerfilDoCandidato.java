package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.fonte.Proposicao;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@UseCase
public class ConsultarPerfilDoCandidato {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPerfilDoCandidato.class);

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

    public PerfilDoCandidato execute(String sq, Integer ano) {
        AppLog.logger(log).system().does("consultar perfil").on("candidato=" + sq).debug();
        Candidato candidato = candidatoRepository.findById(sq)
                .orElseThrow(NotFoundException::new);
        List<Vinculo> vinculos = buscarVinculosDoCandidato.execute(sq);
        List<Proposicao> proposicoes = listarProposicoesDoCandidato.execute(vinculos, ano);
        AppLog.logger(log).system().does("consultar perfil").on("candidato=" + sq).info();
        return new PerfilDoCandidato(candidato, vinculos, proposicoes);
    }
}
