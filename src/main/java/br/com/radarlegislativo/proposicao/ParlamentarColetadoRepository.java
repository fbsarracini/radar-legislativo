package br.com.radarlegislativo.proposicao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParlamentarColetadoRepository extends JpaRepository<ParlamentarColetado, Long> {

    Optional<ParlamentarColetado> findByFonteAndIdExterno(String fonte, String idExterno);

    List<ParlamentarColetado> findByFonteAndIdExternoIn(String fonte, Collection<String> idsExternos);
}
