package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.proposicao.ParlamentarColetado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Ligação entre um candidato do TSE e o cadastro dele numa casa legislativa.
 * A ligação é feita por nome normalizado + UF + cargo, então traz o grau de confiança;
 * homônimos acontecem, e por isso a confirmação manual existe.
 */
@Entity
@Table(name = "vinculo")
public class Vinculo {

    /** Do mais para o menos confiável: a ordem do enum é usada para escolher os melhores. */
    public enum Confianca { NOME_URNA_EXATO, NOME_CIVIL_EXATO, NOME_PARCIAL }

    public enum Status { SUGERIDO, CONFIRMADO, REJEITADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sq_candidato")
    private Candidato candidato;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parlamentar_id")
    private ParlamentarColetado parlamentar;

    @Enumerated(EnumType.STRING)
    @Column(name = "confianca", nullable = false)
    private Confianca confianca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "decidido_em")
    private Instant decididoEm;

    @Deprecated
    protected Vinculo() {}

    public Vinculo(Candidato candidato, ParlamentarColetado parlamentar, Confianca confianca, Instant agora) {
        Assert.notNull(candidato, "o candidato é obrigatório");
        Assert.notNull(parlamentar, "o parlamentar é obrigatório");
        Assert.notNull(confianca, "a confiança é obrigatória");
        this.candidato = candidato;
        this.parlamentar = parlamentar;
        this.confianca = confianca;
        this.status = Status.SUGERIDO;
        this.criadoEm = agora;
    }

    /** O recálculo só mexe em sugestões; decisões manuais ficam como estão. */
    public void recalcular(Confianca nova) {
        if (status == Status.SUGERIDO) this.confianca = nova;
    }

    public void decidir(boolean confirmado, Instant agora) {
        this.status = confirmado ? Status.CONFIRMADO : Status.REJEITADO;
        this.decididoEm = agora;
    }

    public boolean sugerido() {
        return status == Status.SUGERIDO;
    }

    /**
     * Os vínculos que valem para buscar projetos: os confirmados, se houver;
     * senão as sugestões do grau de confiança mais alto. Rejeitados nunca.
     */
    public static List<Vinculo> validos(List<Vinculo> vinculos) {
        List<Vinculo> confirmados = vinculos.stream().filter(v -> v.status == Status.CONFIRMADO).toList();
        if (!confirmados.isEmpty()) return confirmados;
        List<Vinculo> sugeridos = vinculos.stream().filter(Vinculo::sugerido).toList();
        return sugeridos.stream()
                .map(Vinculo::confianca)
                .min(Comparator.naturalOrder())
                .map(melhor -> sugeridos.stream().filter(v -> v.confianca == melhor).toList())
                .orElse(List.of());
    }

    public Long id() { return id; }
    public Candidato candidato() { return candidato; }
    public ParlamentarColetado parlamentar() { return parlamentar; }
    public Confianca confianca() { return confianca; }
    public Status status() { return status; }
    public Instant criadoEm() { return criadoEm; }
    public Instant decididoEm() { return decididoEm; }
}
