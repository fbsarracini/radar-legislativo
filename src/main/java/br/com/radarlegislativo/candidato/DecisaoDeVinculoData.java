package br.com.radarlegislativo.candidato;

public interface DecisaoDeVinculoData {

    /** true = é a mesma pessoa; false = homônimo, não sugerir de novo. */
    Boolean confirmado();
}
