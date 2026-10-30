package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.Esfera;
import org.springframework.util.Assert;

/** Candidato eleito, vindo do arquivo consulta_cand do Portal de Dados Abertos do TSE. */
public record CandidatoEleito(
        String sqCandidato,
        int anoEleicao,
        String nome,
        String nomeUrna,
        String cargo,
        String uf,
        String municipio,
        String partido,
        String numero,
        String situacao) {

    public CandidatoEleito {
        Assert.hasText(sqCandidato, "o sq do candidato é obrigatório");
        Assert.isTrue(anoEleicao > 0, "o ano da eleição deve ser positivo");
    }

    public Esfera esferaDoMandato() {
        String c = cargo == null ? "" : cargo.toUpperCase();
        if (c.contains("DEPUTADO FEDERAL")) return Esfera.CAMARA_FEDERAL;
        if (c.contains("SENADOR")) return Esfera.SENADO_FEDERAL;
        if (c.contains("DEPUTADO ESTADUAL") || c.contains("DEPUTADO DISTRITAL")) return Esfera.ASSEMBLEIA_ESTADUAL;
        if (c.contains("VEREADOR")) return Esfera.CAMARA_MUNICIPAL;
        return null; // cargos executivos (presidente, governador, prefeito) não protocolam projetos como autores parlamentares
    }
}
