package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Parlamentar;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Ligação entre um candidato do TSE e o cadastro dele numa casa legislativa.
 * A ligação é feita por nome normalizado + UF, então traz o grau de confiança.
 */
public record Vinculo(Candidato candidato, Parlamentar parlamentar, Confianca confianca) {

    public enum Confianca { NOME_URNA_EXATO, NOME_CIVIL_EXATO, NOME_PARCIAL }

    public Vinculo {
        Assert.notNull(candidato, "o candidato é obrigatório");
        Assert.notNull(parlamentar, "o parlamentar é obrigatório");
        Assert.notNull(confianca, "a confiança é obrigatória");
    }

    /** Só os vínculos do grau de confiança mais alto presente na lista. */
    public static List<Vinculo> maisConfiaveis(List<Vinculo> vinculos) {
        return vinculos.stream()
                .map(Vinculo::confianca)
                .min(Enum::compareTo)
                .map(melhor -> vinculos.stream().filter(v -> v.confianca() == melhor).toList())
                .orElse(List.of());
    }
}
