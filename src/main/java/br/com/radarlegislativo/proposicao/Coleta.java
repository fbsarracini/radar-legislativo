package br.com.radarlegislativo.proposicao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;

/** Uma execução da coleta numa casa, cobrindo as proposições apresentadas entre {@code de} e {@code ate}. */
@Entity
@Table(name = "coleta")
public class Coleta {

    public enum Status { EM_ANDAMENTO, SUCESSO, FALHA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fonte", nullable = false)
    private String fonte;

    @Column(name = "de", nullable = false)
    private LocalDate de;

    @Column(name = "ate", nullable = false)
    private LocalDate ate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "novas", nullable = false)
    private int novas;

    @Column(name = "existentes", nullable = false)
    private int existentes;

    @Column(name = "erro")
    private String erro;

    @Column(name = "iniciada_em", nullable = false)
    private Instant iniciadaEm;

    @Column(name = "terminada_em")
    private Instant terminadaEm;

    @Deprecated
    protected Coleta() {}

    public Coleta(String fonte, LocalDate de, LocalDate ate, Instant agora) {
        Assert.hasText(fonte, "a fonte é obrigatória");
        Assert.notNull(de, "o início da janela é obrigatório");
        Assert.notNull(ate, "o fim da janela é obrigatório");
        Assert.isTrue(!de.isAfter(ate), "o início da janela deve ser antes do fim");
        this.fonte = fonte;
        this.de = de;
        this.ate = ate;
        this.status = Status.EM_ANDAMENTO;
        this.iniciadaEm = agora;
    }

    public void contarNova() {
        novas++;
    }

    public void contarExistente() {
        existentes++;
    }

    public void concluir(Instant agora) {
        Assert.state(status == Status.EM_ANDAMENTO, "a coleta já terminou");
        this.status = Status.SUCESSO;
        this.terminadaEm = agora;
    }

    public void falhar(String motivo, Instant agora) {
        Assert.state(status == Status.EM_ANDAMENTO, "a coleta já terminou");
        this.status = Status.FALHA;
        this.erro = motivo;
        this.terminadaEm = agora;
    }

    public Long id() { return id; }
    public String fonte() { return fonte; }
    public LocalDate de() { return de; }
    public LocalDate ate() { return ate; }
    public Status status() { return status; }
    public int novas() { return novas; }
    public int existentes() { return existentes; }
    public String erro() { return erro; }
    public Instant iniciadaEm() { return iniciadaEm; }
    public Instant terminadaEm() { return terminadaEm; }
}
