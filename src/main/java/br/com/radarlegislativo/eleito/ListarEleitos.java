package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@UseCase
public class ListarEleitos {

    private static final Logger log = LoggerFactory.getLogger(ListarEleitos.class);

    private final EleitoRepository eleitoRepository;

    public ListarEleitos(EleitoRepository eleitoRepository) {
        this.eleitoRepository = eleitoRepository;
    }

    public Page<CandidatoEleito> execute(FiltroEleitos filtro, Pageable pageable) {
        AppLog.logger(log).system().does("listar eleitos").on(filtro.toString()).debug();
        List<CandidatoEleito> todos = eleitoRepository.buscar(filtro);
        int inicio = (int) Math.min(pageable.getOffset(), todos.size());
        int fim = Math.min(inicio + pageable.getPageSize(), todos.size());
        Page<CandidatoEleito> result = new PageImpl<>(todos.subList(inicio, fim), pageable, todos.size());
        AppLog.logger(log).system().does("listar eleitos").on(filtro + " total=" + todos.size()).info();
        return result;
    }
}
