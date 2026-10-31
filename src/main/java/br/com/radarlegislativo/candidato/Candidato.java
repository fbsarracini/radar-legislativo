package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.Nomes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
 * Candidato de uma eleição, vindo do arquivo consulta_cand do Portal de Dados Abertos do TSE.
 * Guardamos todos, não só os eleitos: quem perdeu a reeleição segue protocolando até o fim do mandato.
 */
@Entity
@Table(name = "candidato")
public class Candidato {

    @Id
    @Column(name = "sq_candidato")
    private String sqCandidato;

    @Column(name = "ano_eleicao", nullable = false)
    private int anoEleicao;

    @Column(name = "nome")
    private String nome;

    @Column(name = "nome_urna")
    private String nomeUrna;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "uf")
    private String uf;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "partido")
    private String partido;

    @Column(name = "numero")
    private String numero;

    @Column(name = "situacao")
    private String situacao;

    @Column(name = "eleito", nullable = false)
    private boolean eleito;

    /** Nome de urna e nome civil normalizados ({@link Nomes#normalizar}), para busca sem acento. */
    @Column(name = "nome_busca", nullable = false)
    private String nomeBusca;

    @Column(name = "cargo_busca", nullable = false)
    private String cargoBusca;

    @Column(name = "municipio_busca", nullable = false)
    private String municipioBusca;

    @Deprecated
    protected Candidato() {}

    public Candidato(String sqCandidato, int anoEleicao, String nome, String nomeUrna, String cargo,
                     String uf, String municipio, String partido, String numero, String situacao) {
        Assert.hasText(sqCandidato, "o sq do candidato é obrigatório");
        Assert.isTrue(anoEleicao > 0, "o ano da eleição deve ser positivo");
        this.sqCandidato = sqCandidato;
        this.anoEleicao = anoEleicao;
        this.nome = nome;
        this.nomeUrna = nomeUrna;
        this.cargo = cargo;
        this.uf = uf;
        this.municipio = municipio;
        this.partido = partido;
        this.numero = numero;
        this.situacao = situacao;
        // DS_SIT_TOT_TURNO traz "ELEITO", "ELEITO POR QP" ou "ELEITO POR MÉDIA" depois da totalização.
        this.eleito = situacao != null && situacao.toUpperCase().startsWith("ELEITO");
        // O separador '|' nunca aparece num termo normalizado, então a busca não casa "atravessando" os dois nomes.
        this.nomeBusca = Nomes.normalizar(nomeUrna) + "|" + Nomes.normalizar(nome);
        this.cargoBusca = Nomes.normalizar(cargo);
        this.municipioBusca = Nomes.normalizar(municipio);
    }

    public Esfera esferaDoMandato() {
        String c = cargo == null ? "" : cargo.toUpperCase();
        if (c.contains("DEPUTADO FEDERAL")) return Esfera.CAMARA_FEDERAL;
        if (c.contains("SENADOR")) return Esfera.SENADO_FEDERAL;
        if (c.contains("DEPUTADO ESTADUAL") || c.contains("DEPUTADO DISTRITAL")) return Esfera.ASSEMBLEIA_ESTADUAL;
        if (c.contains("VEREADOR")) return Esfera.CAMARA_MUNICIPAL;
        return null; // cargos executivos (presidente, governador, prefeito) não protocolam projetos como autores parlamentares
    }

    /** O inverso de {@link #esferaDoMandato()}: os cargos (normalizados) de quem exerce mandato na esfera. */
    public static List<String> cargosDa(Esfera esfera) {
        return switch (esfera) {
            case CAMARA_FEDERAL -> List.of("DEPUTADO FEDERAL");
            case SENADO_FEDERAL -> List.of("SENADOR");
            case ASSEMBLEIA_ESTADUAL -> List.of("DEPUTADO ESTADUAL", "DEPUTADO DISTRITAL");
            case CAMARA_MUNICIPAL -> List.of("VEREADOR");
        };
    }

    public String sqCandidato() { return sqCandidato; }
    public int anoEleicao() { return anoEleicao; }
    public String nome() { return nome; }
    public String nomeUrna() { return nomeUrna; }
    public String cargo() { return cargo; }
    public String uf() { return uf; }
    public String municipio() { return municipio; }
    public String partido() { return partido; }
    public String numero() { return numero; }
    public String situacao() { return situacao; }
    public boolean eleito() { return eleito; }
    String nomeBusca() { return nomeBusca; }
    String cargoBusca() { return cargoBusca; }
    String municipioBusca() { return municipioBusca; }

    @Override
    public boolean equals(Object o) {
        // Accessor, não campo: o outro lado pode ser um proxy lazy do Hibernate, cujos campos ficam nulos.
        return o instanceof Candidato c && Objects.equals(sqCandidato, c.sqCandidato());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sqCandidato);
    }
}
