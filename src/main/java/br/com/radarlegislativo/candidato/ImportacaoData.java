package br.com.radarlegislativo.candidato;

public interface ImportacaoData {

    Integer ano();

    /** Zip ou CSV já baixado, relativo ao diretório de dados do TSE; nulo = baixar do TSE. */
    String arquivo();
}
