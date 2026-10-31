package br.com.radarlegislativo.candidato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CandidatoRepository extends JpaRepository<Candidato, String>,
        JpaSpecificationExecutor<Candidato>, SubstituicaoDeCandidatos {
}
