package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.exception.NotFoundException;
import br.com.radarlegislativo.fonte.Proposicao;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@UseCase
public class ConsultarPerfilDoEleito {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPerfilDoEleito.class);

    private final EleitoRepository eleitoRepository;
    private final BuscarVinculosDoEleito buscarVinculosDoEleito;
    private final ListarProposicoesDoEleito listarProposicoesDoEleito;

    public ConsultarPerfilDoEleito(EleitoRepository eleitoRepository,
                                   BuscarVinculosDoEleito buscarVinculosDoEleito,
                                   ListarProposicoesDoEleito listarProposicoesDoEleito) {
        this.eleitoRepository = eleitoRepository;
        this.buscarVinculosDoEleito = buscarVinculosDoEleito;
        this.listarProposicoesDoEleito = listarProposicoesDoEleito;
    }

    public PerfilDoEleito execute(String sq, Integer ano) {
        AppLog.logger(log).system().does("consultar perfil").on("eleito=" + sq).debug();
        CandidatoEleito eleito = eleitoRepository.buscarPorSq(sq)
                .orElseThrow(NotFoundException::new);
        List<Vinculo> vinculos = buscarVinculosDoEleito.execute(sq);
        List<Proposicao> proposicoes = listarProposicoesDoEleito.execute(vinculos, ano);
        AppLog.logger(log).system().does("consultar perfil").on("eleito=" + sq).info();
        return new PerfilDoEleito(eleito, vinculos, proposicoes);
    }
}
