package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** As 20 coletas mais recentes, de todas as casas — para acompanhar se o agendamento está rodando. */
@UseCase
public class ListarColetas {

    private final ColetaRepository coletaRepository;

    public ListarColetas(ColetaRepository coletaRepository) {
        this.coletaRepository = coletaRepository;
    }

    @Transactional(readOnly = true)
    public List<Coleta> execute() {
        return coletaRepository.findTop20ByOrderByIniciadaEmDesc();
    }
}
