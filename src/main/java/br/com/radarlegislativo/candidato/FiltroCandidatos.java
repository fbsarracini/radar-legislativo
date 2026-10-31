package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Nomes;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Critérios opcionais da listagem de candidatos; campo nulo ou vazio não filtra. */
public record FiltroCandidatos(Integer ano, String uf, String cargo, String nome, String municipio, Boolean eleito) {

    public Specification<Candidato> especificacao() {
        return (root, query, cb) -> {
            List<Predicate> criterios = new ArrayList<>();
            if (ano != null) criterios.add(cb.equal(root.get("anoEleicao"), ano));
            if (uf != null && !uf.isBlank()) criterios.add(cb.equal(root.get("uf"), uf.toUpperCase()));
            if (eleito != null) criterios.add(cb.equal(root.get("eleito"), eleito));
            if (!vazio(cargo)) criterios.add(cb.like(root.get("cargoBusca"), contendo(cargo)));
            if (!vazio(municipio)) criterios.add(cb.equal(root.get("municipioBusca"), Nomes.normalizar(municipio)));
            if (!vazio(nome)) criterios.add(cb.like(root.get("nomeBusca"), contendo(nome)));
            return cb.and(criterios.toArray(Predicate[]::new));
        };
    }

    /** O termo normalizado só tem [A-Z0-9 ], então não carrega curingas do LIKE. */
    private static String contendo(String termo) {
        return "%" + Nomes.normalizar(termo) + "%";
    }

    private static boolean vazio(String termo) {
        return Nomes.normalizar(termo).isEmpty();
    }
}
