package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Escritas da coleta, cada uma na própria transação. Fica fora de {@link ColetarProposicoes}
 * para que as chamadas HTTP às casas aconteçam sem segurar conexão com o banco.
 */
@Component
class GravadorDeProposicoes {

    private final ProposicaoColetadaRepository proposicoes;
    private final ParlamentarColetadoRepository parlamentares;

    GravadorDeProposicoes(ProposicaoColetadaRepository proposicoes, ParlamentarColetadoRepository parlamentares) {
        this.proposicoes = proposicoes;
        this.parlamentares = parlamentares;
    }

    /** @return false se a proposição ainda não foi gravada (e então precisa dos autores) */
    @Transactional
    public boolean atualizarSeExistir(Proposicao proposicao, Instant agora) {
        Optional<ProposicaoColetada> existente = proposicoes.findByFonteAndIdExterno(proposicao.fonte(), proposicao.id());
        existente.ifPresent(p -> p.atualizar(proposicao, agora));
        return existente.isPresent();
    }

    @Transactional(readOnly = true)
    public Set<String> parlamentaresDesconhecidos(String fonte, Collection<String> idsExternos) {
        if (idsExternos.isEmpty()) return Set.of();
        Set<String> desconhecidos = new HashSet<>(idsExternos);
        parlamentares.findByFonteAndIdExternoIn(fonte, idsExternos).forEach(p -> desconhecidos.remove(p.idExterno()));
        return desconhecidos;
    }

    @Transactional
    public void registrarParlamentar(Parlamentar parlamentar, Instant agora) {
        parlamentares.findByFonteAndIdExterno(parlamentar.fonte(), parlamentar.id())
                .ifPresentOrElse(p -> p.atualizar(parlamentar, agora),
                        () -> parlamentares.save(new ParlamentarColetado(parlamentar, agora)));
    }

    @Transactional
    public ProposicaoColetada criar(Proposicao proposicao, List<Autor> autores, Instant agora) {
        Map<String, ParlamentarColetado> porId = parlamentares
                .findByFonteAndIdExternoIn(proposicao.fonte(), ColetarProposicoes.idsDosParlamentares(autores)).stream()
                .collect(Collectors.toMap(ParlamentarColetado::idExterno, Function.identity()));
        return proposicoes.save(new ProposicaoColetada(proposicao, autores, porId, agora));
    }
}
