package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class ListarCandidatos {

    private static final Logger log = LoggerFactory.getLogger(ListarCandidatos.class);

    /** Ordem fixa: aceitar "sort" do cliente exporia os nomes dos campos internos. */
    static final Sort POR_NOME_URNA = Sort.by("nomeUrna", "sqCandidato");

    private final CandidatoRepository candidatoRepository;

    public ListarCandidatos(CandidatoRepository candidatoRepository) {
        this.candidatoRepository = candidatoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Candidato> execute(FiltroCandidatos filtro, Pageable pageable) {
        AppLog.logger(log).system().does("listar candidatos").on(filtro.toString()).debug();
        Pageable pagina = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), POR_NOME_URNA);
        Page<Candidato> result = candidatoRepository.findAll(filtro.especificacao(), pagina);
        AppLog.logger(log).system().does("listar candidatos").on(filtro + " total=" + result.getTotalElements()).info();
        return result;
    }
}
