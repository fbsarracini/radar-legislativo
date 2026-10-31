package br.com.radarlegislativo.candidato;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VinculoRepository extends JpaRepository<Vinculo, Long> {

    /** Sem ORDER BY: a confiança é gravada como texto, e a ordem certa é a do enum (feita no Java). */
    @EntityGraph(attributePaths = "parlamentar")
    List<Vinculo> findByCandidatoSqCandidato(String sqCandidato);

    @EntityGraph(attributePaths = "parlamentar")
    Optional<Vinculo> findByIdAndCandidatoSqCandidato(Long id, String sqCandidato);

    List<Vinculo> findByParlamentarId(Long parlamentarId);

    /** Selo do feed: as candidaturas dos autores de uma página. */
    @EntityGraph(attributePaths = "candidato")
    List<Vinculo> findByParlamentarIdInAndStatusNot(Collection<Long> parlamentarIds, Vinculo.Status status);
}
