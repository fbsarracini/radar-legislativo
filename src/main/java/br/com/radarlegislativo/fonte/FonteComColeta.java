package br.com.radarlegislativo.fonte;

import java.time.LocalDate;
import java.util.List;

/**
 * Casa cuja API permite listar o que foi protocolado num período — é o que alimenta a coleta agendada.
 * Autores e parlamentar ficam em chamadas separadas porque, na maioria das APIs, cada um é um request a mais:
 * a coleta só os pede para proposições e parlamentares que ainda não conhece.
 */
public interface FonteComColeta extends FonteLegislativa {

    /** Proposições apresentadas entre {@code de} e {@code ate}, inclusive, dos tipos configurados. */
    List<Proposicao> proposicoesApresentadasEntre(LocalDate de, LocalDate ate);

    List<Autor> autoresDa(String idProposicao);

    Parlamentar parlamentar(String idParlamentar);
}
