package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Nomes;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Regras de casamento por nome entre o cadastro de uma casa e os candidatos do TSE. */
final class Casamento {

    /** Acima disso, um casamento só parcial ("Ana Paula" ⊂ vários nomes civis) é ambíguo demais para sugerir. */
    static final int MAX_PARCIAIS = 3;

    private Casamento() {}

    static Optional<Vinculo.Confianca> classificar(Candidato c, String nomeNaCasa) {
        String nomeCasa = Nomes.chave(nomeNaCasa);
        if (!nomeCasa.isEmpty() && nomeCasa.equals(Nomes.chave(c.nomeUrna()))) {
            return Optional.of(Vinculo.Confianca.NOME_URNA_EXATO);
        }
        if (!nomeCasa.isEmpty() && nomeCasa.equals(Nomes.chave(c.nome()))) {
            return Optional.of(Vinculo.Confianca.NOME_CIVIL_EXATO);
        }
        if (Nomes.contidoEm(nomeNaCasa, c.nome()) || Nomes.contidoEm(c.nomeUrna(), nomeNaCasa)) {
            return Optional.of(Vinculo.Confianca.NOME_PARCIAL);
        }
        return Optional.empty();
    }

    /**
     * Em cada eleição, só os candidatos do grau de confiança mais alto encontrado:
     * se há um nome de urna exato, os parciais daquele ano são ruído.
     */
    static Map<Candidato, Vinculo.Confianca> sugerir(String nomeNaCasa, List<Candidato> candidatos) {
        Map<Integer, Map<Candidato, Vinculo.Confianca>> porAno = new LinkedHashMap<>();
        for (Candidato c : candidatos) {
            classificar(c, nomeNaCasa).ifPresent(conf ->
                    porAno.computeIfAbsent(c.anoEleicao(), a -> new LinkedHashMap<>()).put(c, conf));
        }
        Map<Candidato, Vinculo.Confianca> result = new LinkedHashMap<>();
        for (Map<Candidato, Vinculo.Confianca> doAno : porAno.values()) {
            Vinculo.Confianca melhor = doAno.values().stream().min(Comparator.naturalOrder()).orElseThrow();
            Map<Candidato, Vinculo.Confianca> melhores = doAno.entrySet().stream()
                    .filter(e -> e.getValue() == melhor)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            if (melhor == Vinculo.Confianca.NOME_PARCIAL && melhores.size() > MAX_PARCIAIS) continue;
            result.putAll(melhores);
        }
        return result;
    }
}
