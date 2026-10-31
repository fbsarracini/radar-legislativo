package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.Proposicao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Proposição gravada pela coleta; (fonte, idExterno) identifica a mesma proposição entre coletas. */
@Entity
@Table(name = "proposicao")
public class ProposicaoColetada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fonte", nullable = false)
    private String fonte;

    @Column(name = "id_externo", nullable = false)
    private String idExterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "esfera", nullable = false)
    private Esfera esfera;

    @Column(name = "casa")
    private String casa;

    @Column(name = "sigla")
    private String sigla;

    @Column(name = "numero")
    private Integer numero;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "ementa")
    private String ementa;

    @Column(name = "data_apresentacao")
    private LocalDate dataApresentacao;

    @Column(name = "url")
    private String url;

    @Column(name = "primeira_vez_vista_em", nullable = false)
    private Instant primeiraVezVistaEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    // Lote: o feed carrega os autores da página inteira em poucas queries, sem N+1.
    @OneToMany(mappedBy = "proposicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    @BatchSize(size = 100)
    private List<Autoria> autores = new ArrayList<>();

    @Deprecated
    protected ProposicaoColetada() {}

    /**
     * @param parlamentares parlamentares já gravados, por id na casa; autor sem entrada aqui fica sem vínculo
     */
    public ProposicaoColetada(Proposicao proposicao, List<Autor> autores,
                              Map<String, ParlamentarColetado> parlamentares, Instant agora) {
        Assert.notNull(proposicao, "a proposição é obrigatória");
        Assert.hasText(proposicao.id(), "o id da proposição na casa é obrigatório");
        Assert.notNull(autores, "os autores são obrigatórios");
        this.fonte = proposicao.fonte();
        this.idExterno = proposicao.id();
        this.primeiraVezVistaEm = agora;
        atualizar(proposicao, agora);
        for (int i = 0; i < autores.size(); i++) {
            Autor autor = autores.get(i);
            ParlamentarColetado parlamentar = autor.idParlamentar() == null ? null : parlamentares.get(autor.idParlamentar());
            this.autores.add(new Autoria(this, i + 1, autor, parlamentar));
        }
    }

    /** A casa pode corrigir ementa, número etc.; autores e primeira vez vista não mudam. */
    public void atualizar(Proposicao proposicao, Instant agora) {
        Assert.isTrue(fonte.equals(proposicao.fonte()) && idExterno.equals(proposicao.id()), "é outra proposição");
        this.esfera = proposicao.esfera();
        this.casa = proposicao.casa();
        this.sigla = proposicao.sigla();
        this.numero = proposicao.numero();
        this.ano = proposicao.ano();
        this.ementa = proposicao.ementa();
        this.dataApresentacao = proposicao.dataApresentacao();
        this.url = proposicao.url();
        this.atualizadoEm = agora;
    }

    /** Ex.: "PL 1234/2025" — mesmo formato de {@link Proposicao#identificacao()}. */
    public String identificacao() {
        if (sigla == null) {
            return null;
        }
        return sigla + (numero != null ? " " + numero : "") + (ano != null ? "/" + ano : "");
    }

    /** Inicializa autores e parlamentares (em lote) para uso fora da transação. */
    void carregarAutores() {
        autores.forEach(a -> Hibernate.initialize(a.parlamentar()));
    }

    public Long id() { return id; }
    public String fonte() { return fonte; }
    public String idExterno() { return idExterno; }
    public Esfera esfera() { return esfera; }
    public String casa() { return casa; }
    public String sigla() { return sigla; }
    public Integer numero() { return numero; }
    public Integer ano() { return ano; }
    public String ementa() { return ementa; }
    public LocalDate dataApresentacao() { return dataApresentacao; }
    public String url() { return url; }
    public Instant primeiraVezVistaEm() { return primeiraVezVistaEm; }
    public Instant atualizadoEm() { return atualizadoEm; }
    public List<Autoria> autores() { return Collections.unmodifiableList(autores); }
}
