package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.fonte.Autor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.util.Assert;

/** Um autor de uma proposição coletada, ligado ao parlamentar quando ele é da casa. */
@Entity
@Table(name = "proposicao_autor")
public class Autoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proposicao_id")
    private ProposicaoColetada proposicao;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "nome")
    private String nome;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "proponente", nullable = false)
    private boolean proponente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parlamentar_id")
    private ParlamentarColetado parlamentar;

    @Deprecated
    protected Autoria() {}

    /** @param parlamentar nulo quando o autor não é parlamentar da casa */
    Autoria(ProposicaoColetada proposicao, int ordem, Autor autor, ParlamentarColetado parlamentar) {
        Assert.notNull(proposicao, "a proposição é obrigatória");
        Assert.notNull(autor, "o autor é obrigatório");
        Assert.isTrue(parlamentar == null || parlamentar.idExterno().equals(autor.idParlamentar()),
                "o parlamentar não corresponde ao autor");
        this.proposicao = proposicao;
        this.ordem = ordem;
        this.nome = autor.nome();
        this.tipo = autor.tipo();
        this.proponente = autor.proponente();
        this.parlamentar = parlamentar;
    }

    public int ordem() { return ordem; }
    public String nome() { return nome; }
    public String tipo() { return tipo; }
    public boolean proponente() { return proponente; }
    public ParlamentarColetado parlamentar() { return parlamentar; }
}
