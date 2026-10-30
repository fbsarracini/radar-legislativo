package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.UseCase;
import br.com.radarlegislativo.config.RadarProperties;
import br.com.radarlegislativo.log.AppLog;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Carrega os eleitos de um ano (baixando do TSE ou lendo um arquivo do diretório de dados). */
@UseCase
public class ImportarEleitos {

    private static final Logger log = LoggerFactory.getLogger(ImportarEleitos.class);

    private final ImportadorTse importador;
    private final EleitoRepository eleitoRepository;
    private final Path diretorioDados;

    public ImportarEleitos(ImportadorTse importador, EleitoRepository eleitoRepository, RadarProperties props) {
        this.importador = importador;
        this.eleitoRepository = eleitoRepository;
        this.diretorioDados = Path.of(props.tse().diretorioCache()).toAbsolutePath().normalize();
    }

    public int execute(@Valid ImportacaoData dados) {
        int ano = dados.ano();
        AppLog.logger(log).system().does("importar eleitos").on("ano=" + ano).debug();
        List<CandidatoEleito> eleitos = dados.arquivo() == null || dados.arquivo().isBlank()
                ? importador.importar(ano)
                : importador.importarArquivo(dentroDoDiretorioDeDados(dados.arquivo()), ano);
        eleitoRepository.substituirAno(ano, eleitos);
        AppLog.logger(log).system().does("importar eleitos").on("ano=" + ano + " total=" + eleitos.size()).info();
        return eleitos.size();
    }

    /** Impede que o endpoint vire leitor de qualquer arquivo do servidor (../../etc/...). */
    private Path dentroDoDiretorioDeDados(String arquivo) {
        Path caminho = diretorioDados.resolve(arquivo).normalize();
        Assert.isTrue(caminho.startsWith(diretorioDados), "o arquivo deve estar no diretório de dados do TSE");
        Assert.isTrue(Files.isRegularFile(caminho), "arquivo não encontrado no diretório de dados do TSE");
        return caminho;
    }
}
