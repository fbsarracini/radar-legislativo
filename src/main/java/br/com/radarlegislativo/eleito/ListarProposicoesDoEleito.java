package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.fonte.ListarProposicoesDoParlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** Projetos de todos os vínculos de maior confiança do eleito. */
@UseCase
public class ListarProposicoesDoEleito {

    private static final Logger log = LoggerFactory.getLogger(ListarProposicoesDoEleito.class);

    private final BuscarVinculosDoEleito buscarVinculosDoEleito;
    private final ListarProposicoesDoParlamentar listarProposicoesDoParlamentar;

    public ListarProposicoesDoEleito(BuscarVinculosDoEleito buscarVinculosDoEleito,
                                     ListarProposicoesDoParlamentar listarProposicoesDoParlamentar) {
        this.buscarVinculosDoEleito = buscarVinculosDoEleito;
        this.listarProposicoesDoParlamentar = listarProposicoesDoParlamentar;
    }

    public List<Proposicao> execute(String sq, Integer ano) {
        return execute(buscarVinculosDoEleito.execute(sq), ano);
    }

    /** Variante para quem já buscou os vínculos (evita refazer o casamento por nome). */
    public List<Proposicao> execute(List<Vinculo> vinculos, Integer ano) {
        List<Vinculo> melhores = Vinculo.maisConfiaveis(vinculos);
        AppLog.logger(log).system().does("listar proposições do eleito").on("vinculos=" + melhores.size() + " ano=" + ano).debug();
        List<Proposicao> result = melhores.stream()
                .flatMap(v -> listarProposicoesDoParlamentar.execute(v.parlamentar().fonte(), v.parlamentar().id(), ano).stream())
                .distinct()
                .toList();
        AppLog.logger(log).system().does("listar proposições do eleito").on("vinculos=" + melhores.size() + " total=" + result.size()).info();
        return result;
    }
}
