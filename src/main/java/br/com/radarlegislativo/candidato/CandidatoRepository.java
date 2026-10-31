package br.com.radarlegislativo.candidato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface CandidatoRepository extends JpaRepository<Candidato, String>,
        JpaSpecificationExecutor<Candidato>, SubstituicaoDeCandidatos {

    /** Candidatos que disputaram um dos cargos numa UF, em qualquer eleição importada — base do casamento. */
    List<Candidato> findByUfAndCargoBuscaIn(String uf, Collection<String> cargos);

    List<Candidato> findByUfAndCargoBuscaInAndMunicipioBusca(String uf, Collection<String> cargos, String municipio);
}
