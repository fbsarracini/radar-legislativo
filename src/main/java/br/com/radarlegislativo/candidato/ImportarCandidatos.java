package br.com.radarlegislativo.candidato;

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

/** Carrega os candidatos de um ano (baixando do TSE ou lendo um arquivo do diretório de dados). */
@UseCase
public class ImportarCandidatos {

    private static final Logger log = LoggerFactory.getLogger(ImportarCandidatos.class);

    private final ImportadorTse importador;
    private final CandidatoRepository candidatoRepository;
    private final Path diretorioDados;

    public ImportarCandidatos(ImportadorTse importador, CandidatoRepository candidatoRepository, RadarProperties props) {
        this.importador = importador;
        this.candidatoRepository = candidatoRepository;
        this.diretorioDados = Path.of(props.tse().diretorioCache()).toAbsolutePath().normalize();
    }

    public int execute(@Valid ImportacaoData dados) {
        int ano = dados.ano();
        AppLog.logger(log).system().does("importar candidatos").on("ano=" + ano).debug();
        List<Candidato> candidatos = dados.arquivo() == null || dados.arquivo().isBlank()
                ? importador.importar(ano)
                : importador.importarArquivo(dentroDoDiretorioDeDados(dados.arquivo()), ano);
        candidatoRepository.substituirAno(ano, candidatos);
        AppLog.logger(log).system().does("importar candidatos").on("ano=" + ano + " total=" + candidatos.size()).info();
        return candidatos.size();
    }

    /** Impede que o endpoint vire leitor de qualquer arquivo do servidor (../../etc/...). */
    private Path dentroDoDiretorioDeDados(String arquivo) {
        Path caminho = diretorioDados.resolve(arquivo).normalize();
        Assert.isTrue(caminho.startsWith(diretorioDados), "o arquivo deve estar no diretório de dados do TSE");
        Assert.isTrue(Files.isRegularFile(caminho), "arquivo não encontrado no diretório de dados do TSE");
        return caminho;
    }
}
