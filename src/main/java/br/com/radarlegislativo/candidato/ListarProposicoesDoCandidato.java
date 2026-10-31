package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.log.AppLog;
import br.com.radarlegislativo.proposicao.FeedDeProposicoes;
import br.com.radarlegislativo.proposicao.FiltroProposicoes;
import br.com.radarlegislativo.proposicao.ListarProposicoes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/** Projetos coletados de autoria do candidato, pelos vínculos válidos ({@link Vinculo#validos}). */
@UseCase
public class ListarProposicoesDoCandidato {

    private static final Logger log = LoggerFactory.getLogger(ListarProposicoesDoCandidato.class);

    private final BuscarVinculosDoCandidato buscarVinculosDoCandidato;
    private final ListarProposicoes listarProposicoes;

    public ListarProposicoesDoCandidato(BuscarVinculosDoCandidato buscarVinculosDoCandidato,
                                        ListarProposicoes listarProposicoes) {
        this.buscarVinculosDoCandidato = buscarVinculosDoCandidato;
        this.listarProposicoes = listarProposicoes;
    }

    public FeedDeProposicoes execute(String sq, LocalDate desde, LocalDate ate, Pageable pageable) {
        return execute(buscarVinculosDoCandidato.execute(sq), desde, ate, pageable);
    }

    /** Variante para quem já buscou os vínculos. */
    public FeedDeProposicoes execute(List<Vinculo> vinculos, LocalDate desde, LocalDate ate, Pageable pageable) {
        List<Long> parlamentares = Vinculo.validos(vinculos).stream().map(v -> v.parlamentar().id()).distinct().toList();
        AppLog.logger(log).system().does("listar proposições do candidato").on("parlamentares=" + parlamentares).debug();
        if (parlamentares.isEmpty()) return FeedDeProposicoes.vazio(pageable);
        return listarProposicoes.execute(
                new FiltroProposicoes(desde, ate, null, null, null, null, null, null, parlamentares), pageable);
    }
}
