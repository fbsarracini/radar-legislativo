package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.fonte.Nomes;

/** Critérios opcionais da listagem de eleitos; campo nulo ou vazio não filtra. */
public record FiltroEleitos(Integer ano, String uf, String cargo, String nome, String municipio) {

    public boolean aceita(CandidatoEleito e) {
        return (ano == null || e.anoEleicao() == ano)
                && (uf == null || uf.isBlank() || uf.equalsIgnoreCase(e.uf()))
                && contem(e.cargo(), cargo)
                && (vazio(municipio) || Nomes.normalizar(e.municipio()).equals(Nomes.normalizar(municipio)))
                && (vazio(nome) || contem(e.nomeUrna(), nome) || contem(e.nome(), nome));
    }

    private static boolean contem(String valor, String termo) {
        return vazio(termo) || Nomes.normalizar(valor).contains(Nomes.normalizar(termo));
    }

    private static boolean vazio(String termo) {
        return Nomes.normalizar(termo).isEmpty();
    }
}
