package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.candidato.VinculoRepository;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/** O feed: o que foi protocolado, do mais recente para o mais antigo. */
@UseCase
public class ListarProposicoes {

    private static final Logger log = LoggerFactory.getLogger(ListarProposicoes.class);


    private final ProposicaoColetadaRepository proposicaoRepository;
    private final VinculoRepository vinculoRepository;

    public ListarProposicoes(ProposicaoColetadaRepository proposicaoRepository, VinculoRepository vinculoRepository) {
        this.proposicaoRepository = proposicaoRepository;
        this.vinculoRepository = vinculoRepository;
    }

    @Transactional(readOnly = true)
    public FeedDeProposicoes execute(FiltroProposicoes filtro, Pageable pageable) {
        AppLog.logger(log).system().does("listar proposições").on(filtro.toString()).debug();
        // Sem Sort na página: a ordem vem de maisRecentesPrimeiro() e o "sort" do cliente é ignorado.
        Pageable pagina = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<ProposicaoColetada> result = proposicaoRepository.findAll(filtro.especificacao().and(maisRecentesPrimeiro()), pagina);
        // A resposta é montada fora da transação (open-in-view desligado): os autores já saem carregados.
        result.forEach(ProposicaoColetada::carregarAutores);
        Candidaturas candidaturas = Candidaturas.dos(result.getContent(), vinculoRepository);
        AppLog.logger(log).system().does("listar proposições").on(filtro + " total=" + result.getTotalElements()).info();
        return new FeedDeProposicoes(result, candidaturas);
    }

    /**
     * Data de apresentação decrescente, sem data por último (no Postgres, DESC põe NULL primeiro).
     * Fica numa Specification porque o Sort do Spring Data não aceita precedência de nulos em Criteria.
     */
    static Specification<ProposicaoColetada> maisRecentesPrimeiro() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class) { // a consulta de contagem não ordena
                HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
                query.orderBy(hcb.desc(root.get("dataApresentacao"), false), cb.desc(root.get("id")));
            }
            return null;
        };
    }
}
