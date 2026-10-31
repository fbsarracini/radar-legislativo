package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.candidato.Vinculo;
import br.com.radarlegislativo.candidato.VinculoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** O selo do feed: as candidaturas no TSE de cada autor, por id interno do parlamentar. Rejeitados não entram. */
public record Candidaturas(Map<Long, List<Vinculo>> porParlamentar) {

    /** Deve rodar dentro da transação: os candidatos vêm carregados junto (EntityGraph). */
    static Candidaturas dos(Collection<ProposicaoColetada> proposicoes, VinculoRepository vinculoRepository) {
        List<Long> ids = proposicoes.stream()
                .flatMap(p -> p.autores().stream())
                .map(Autoria::parlamentar)
                .filter(Objects::nonNull)
                .map(ParlamentarColetado::id)
                .distinct()
                .toList();
        if (ids.isEmpty()) return new Candidaturas(Map.of());
        return new Candidaturas(vinculoRepository.findByParlamentarIdInAndStatusNot(ids, Vinculo.Status.REJEITADO).stream()
                .collect(Collectors.groupingBy(v -> v.parlamentar().id())));
    }

    public List<Vinculo> de(ParlamentarColetado parlamentar) {
        return parlamentar == null ? List.of() : porParlamentar.getOrDefault(parlamentar.id(), List.of());
    }
}
