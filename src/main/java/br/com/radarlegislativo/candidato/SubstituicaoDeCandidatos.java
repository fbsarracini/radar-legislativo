package br.com.radarlegislativo.candidato;

import java.util.List;

/** Carga em lote dos candidatos de um ano — o arquivo do TSE tem centenas de milhares de linhas. */
public interface SubstituicaoDeCandidatos {

    /** Troca os candidatos do ano numa transação: quem consulta nunca vê o ano vazio ou pela metade. */
    void substituirAno(int ano, List<Candidato> novos);
}
