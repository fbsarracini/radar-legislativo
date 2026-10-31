package br.com.radarlegislativo.proposicao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProposicaoColetadaRepository extends JpaRepository<ProposicaoColetada, Long> {

    Optional<ProposicaoColetada> findByFonteAndIdExterno(String fonte, String idExterno);
}
