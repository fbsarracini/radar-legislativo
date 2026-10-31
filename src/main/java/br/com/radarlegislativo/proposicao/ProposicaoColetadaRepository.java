package br.com.radarlegislativo.proposicao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProposicaoColetadaRepository extends JpaRepository<ProposicaoColetada, Long>,
        JpaSpecificationExecutor<ProposicaoColetada> {

    Optional<ProposicaoColetada> findByFonteAndIdExterno(String fonte, String idExterno);
}
