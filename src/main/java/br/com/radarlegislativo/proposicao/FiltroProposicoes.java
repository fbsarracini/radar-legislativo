package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.Nomes;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Critérios opcionais do feed; campo nulo ou vazio não filtra.
 * {@code uf}, {@code partido}, {@code autor} e {@code parlamentar} valem para o mesmo autor:
 * "partido=PT&uf=SP" é "tem um autor do PT de SP", não "um do PT e outro de SP".
 *
 * @param desde       data de apresentação mínima (inclusive)
 * @param ate         data de apresentação máxima (inclusive)
 * @param novasDesde  só o que a coleta viu pela primeira vez a partir deste instante
 * @param tipos       siglas (PL, PEC...)
 * @param autor       trecho do nome do autor, sem diferenciar acento
 * @param parlamentar id interno do parlamentar autor
 */
public record FiltroProposicoes(LocalDate desde, LocalDate ate, Instant novasDesde, String fonte, List<String> tipos,
                                String uf, String partido, String autor, Long parlamentar) {

    public FiltroProposicoes {
        Assert.isTrue(desde == null || ate == null || !desde.isAfter(ate), "'desde' deve ser antes de 'ate'");
        tipos = tipos == null ? List.of() : tipos.stream().filter(t -> !t.isBlank()).map(t -> t.trim().toUpperCase()).toList();
    }

    public Specification<ProposicaoColetada> especificacao() {
        return (root, query, cb) -> {
            List<Predicate> criterios = new ArrayList<>();
            if (desde != null) criterios.add(cb.greaterThanOrEqualTo(root.get("dataApresentacao"), desde));
            if (ate != null) criterios.add(cb.lessThanOrEqualTo(root.get("dataApresentacao"), ate));
            if (novasDesde != null) criterios.add(cb.greaterThanOrEqualTo(root.get("primeiraVezVistaEm"), novasDesde));
            if (!vazio(fonte)) criterios.add(cb.equal(root.get("fonte"), fonte));
            if (!tipos.isEmpty()) criterios.add(root.get("sigla").in(tipos));
            if (filtraAutor()) {
                Subquery<Long> autoria = query.subquery(Long.class);
                Root<Autoria> a = autoria.from(Autoria.class);
                Join<Autoria, ParlamentarColetado> p = a.join("parlamentar", JoinType.LEFT);
                List<Predicate> doAutor = new ArrayList<>();
                doAutor.add(cb.equal(a.get("proposicao"), root));
                if (!vazio(uf)) doAutor.add(cb.equal(p.get("uf"), uf.toUpperCase()));
                if (!vazio(partido)) doAutor.add(cb.equal(cb.upper(p.get("partido")), partido.trim().toUpperCase()));
                if (parlamentar != null) doAutor.add(cb.equal(p.get("id"), parlamentar));
                // O termo normalizado só tem [A-Z0-9 ], então não carrega curingas do LIKE.
                if (!vazio(autor)) doAutor.add(cb.like(a.get("nomeBusca"), "%" + Nomes.normalizar(autor) + "%"));
                autoria.select(a.get("id")).where(doAutor.toArray(Predicate[]::new));
                criterios.add(cb.exists(autoria));
            }
            return cb.and(criterios.toArray(Predicate[]::new));
        };
    }

    private boolean filtraAutor() {
        return !vazio(uf) || !vazio(partido) || !vazio(autor) || parlamentar != null;
    }

    private static boolean vazio(String termo) {
        return Nomes.normalizar(termo).isEmpty();
    }
}
