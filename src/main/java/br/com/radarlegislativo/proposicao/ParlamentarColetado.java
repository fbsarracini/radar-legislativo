package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.Parlamentar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.Assert;

import java.time.Instant;

/** Parlamentar que apareceu como autor numa coleta, com o id que a própria casa usa. */
@Entity
@Table(name = "parlamentar")
@BatchSize(size = 100)
public class ParlamentarColetado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fonte", nullable = false)
    private String fonte;

    @Column(name = "id_externo", nullable = false)
    private String idExterno;

    @Column(name = "nome")
    private String nome;

    @Column(name = "partido")
    private String partido;

    @Column(name = "uf")
    private String uf;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Deprecated
    protected ParlamentarColetado() {}

    public ParlamentarColetado(Parlamentar parlamentar, Instant agora) {
        Assert.notNull(parlamentar, "o parlamentar é obrigatório");
        this.fonte = parlamentar.fonte();
        this.idExterno = parlamentar.id();
        atualizar(parlamentar, agora);
    }

    public void atualizar(Parlamentar parlamentar, Instant agora) {
        Assert.isTrue(fonte.equals(parlamentar.fonte()) && idExterno.equals(parlamentar.id()), "é outro parlamentar");
        this.nome = parlamentar.nome();
        this.partido = parlamentar.partido();
        this.uf = parlamentar.uf();
        this.fotoUrl = parlamentar.fotoUrl();
        this.atualizadoEm = agora;
    }

    public Long id() { return id; }
    public String fonte() { return fonte; }
    public String idExterno() { return idExterno; }
    public String nome() { return nome; }
    public String partido() { return partido; }
    public String uf() { return uf; }
    public String fotoUrl() { return fotoUrl; }
    public Instant atualizadoEm() { return atualizadoEm; }
}
