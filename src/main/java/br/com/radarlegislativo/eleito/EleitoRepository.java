package br.com.radarlegislativo.eleito;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Eleitos em memória, para manter o projeto simples. Num projeto real,
 * troque por um JpaRepository (PostgreSQL) e grave também os vínculos confirmados.
 */
@Repository
public class EleitoRepository {

    private static final Comparator<CandidatoEleito> POR_NOME_URNA =
            Comparator.comparing(CandidatoEleito::nomeUrna, Comparator.nullsLast(String::compareTo));

    private final Map<String, CandidatoEleito> eleitos = new ConcurrentHashMap<>();

    /** Troca os eleitos do ano sem deixar uma janela em que o ano aparece vazio. */
    public void substituirAno(int ano, List<CandidatoEleito> novos) {
        Set<String> sqs = novos.stream().map(CandidatoEleito::sqCandidato).collect(Collectors.toSet());
        novos.forEach(this::salvar);
        eleitos.values().removeIf(e -> e.anoEleicao() == ano && !sqs.contains(e.sqCandidato()));
    }

    public void salvar(CandidatoEleito eleito) {
        eleitos.put(eleito.sqCandidato(), eleito);
    }

    public Optional<CandidatoEleito> buscarPorSq(String sq) {
        return Optional.ofNullable(eleitos.get(sq));
    }

    public List<CandidatoEleito> buscar(FiltroEleitos filtro) {
        return eleitos.values().stream()
                .filter(filtro::aceita)
                .sorted(POR_NOME_URNA)
                .toList();
    }
}
