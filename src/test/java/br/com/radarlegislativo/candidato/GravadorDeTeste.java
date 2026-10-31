package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import br.com.radarlegislativo.proposicao.ParlamentarColetado;
import br.com.radarlegislativo.proposicao.ParlamentarColetadoRepository;
import br.com.radarlegislativo.proposicao.ProposicaoColetada;
import br.com.radarlegislativo.proposicao.ProposicaoColetadaRepository;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Prepara parlamentares e proposições pelo construtor público das entidades (o gravador da coleta é do pacote dela). */
@TestComponent
class GravadorDeTeste {

    private static final Instant AGORA = Instant.parse("2026-10-31T12:00:00Z");

    private final ParlamentarColetadoRepository parlamentares;
    private final ProposicaoColetadaRepository proposicoes;

    GravadorDeTeste(ParlamentarColetadoRepository parlamentares, ProposicaoColetadaRepository proposicoes) {
        this.parlamentares = parlamentares;
        this.proposicoes = proposicoes;
    }

    @Transactional
    public Long parlamentar(Parlamentar p) {
        return parlamentares.save(new ParlamentarColetado(p, AGORA)).id();
    }

    @Transactional
    public void proposicao(Proposicao p, List<Autor> autores) {
        List<String> ids = autores.stream().map(Autor::idParlamentar).toList();
        Map<String, ParlamentarColetado> porId = parlamentares.findByFonteAndIdExternoIn(p.fonte(), ids).stream()
                .collect(Collectors.toMap(ParlamentarColetado::idExterno, Function.identity()));
        proposicoes.save(new ProposicaoColetada(p, autores, porId, AGORA));
    }
}
