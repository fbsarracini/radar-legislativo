package br.com.radarlegislativo.eleito;

public interface ImportacaoData {

    Integer ano();

    /** Zip ou CSV já baixado, relativo ao diretório de dados do TSE; nulo = baixar do TSE. */
    String arquivo();
}
