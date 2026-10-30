package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@UseCase
public class ListarFontes {

    private static final Logger log = LoggerFactory.getLogger(ListarFontes.class);

    private final FonteRegistry registry;

    public ListarFontes(FonteRegistry registry) {
        this.registry = registry;
    }

    public List<FonteLegislativa> execute() {
        AppLog.logger(log).system().does("listar fontes").debug();
        List<FonteLegislativa> result = registry.todas();
        AppLog.logger(log).system().does("listar fontes").on("total=" + result.size()).info();
        return result;
    }
}
