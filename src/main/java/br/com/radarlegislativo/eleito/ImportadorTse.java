package br.com.radarlegislativo.eleito;

import br.com.radarlegislativo.config.RadarProperties;
import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Importa os eleitos do arquivo consulta_cand_{ano}.zip do Portal de Dados Abertos do TSE.
 * <ul>
 *   <li>Eleições gerais (2022, 2026): presidente, governador, senador, deputados federal/estadual/distrital.</li>
 *   <li>Eleições municipais (2024, 2028): prefeito e vereador.</li>
 * </ul>
 * A coluna DS_SIT_TOT_TURNO traz "ELEITO", "ELEITO POR QP" ou "ELEITO POR MÉDIA" depois da totalização.
 */
@Component
public class ImportadorTse {

    private static final Logger log = LoggerFactory.getLogger(ImportadorTse.class);

    private final RadarProperties.Tse cfg;

    public ImportadorTse(RadarProperties props) {
        this.cfg = props.tse();
    }

    /** Baixa (se ainda não estiver em cache) e lê o zip do ano. */
    public List<CandidatoEleito> importar(int ano) {
        return lerZip(obterZip(ano), ano);
    }

    /** Lê um zip ou csv já presente em disco (útil quando o download é feito por fora). */
    public List<CandidatoEleito> importarArquivo(Path arquivo, int ano) {
        return arquivo.toString().toLowerCase().endsWith(".zip") ? lerZip(arquivo, ano) : lerCsv(arquivo, ano);
    }

    private Path obterZip(int ano) {
        try {
            Path dir = Path.of(cfg.diretorioCache());
            Files.createDirectories(dir);
            Path destino = dir.resolve("consulta_cand_" + ano + ".zip");
            if (Files.exists(destino) && Files.size(destino) > 0) return destino;

            String url = cfg.urlTemplate().replace("{ano}", String.valueOf(ano));
            AppLog.logger(log).system().does("baixar arquivo do TSE").on(url).info();
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20)).build();
            Path temp = Files.createTempFile(dir, "tse", ".part");
            HttpResponse<Path> resp = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofFile(temp));
            if (resp.statusCode() != 200) {
                Files.deleteIfExists(temp);
                throw new IllegalStateException("TSE respondeu HTTP " + resp.statusCode() + " para " + url);
            }
            return Files.move(temp, destino);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao baixar arquivo do TSE", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Download interrompido", e);
        }
    }

    private List<CandidatoEleito> lerZip(Path zip, int ano) {
        Map<String, CandidatoEleito> porSq = new LinkedHashMap<>();
        try (ZipFile zf = new ZipFile(zip.toFile())) {
            List<? extends ZipEntry> csvs = zf.stream().filter(e -> e.getName().toLowerCase().endsWith(".csv")).toList();
            // O zip traz um CSV por UF e um _BRASIL com tudo; se houver o _BRASIL, basta ele.
            List<? extends ZipEntry> alvo = csvs.stream().filter(e -> e.getName().toUpperCase().contains("_BRASIL")).toList();
            if (alvo.isEmpty()) alvo = csvs;
            for (ZipEntry e : alvo) {
                try (InputStream in = zf.getInputStream(e)) {
                    lerStream(in, ano, porSq);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + zip, e);
        }
        AppLog.logger(log).system().does("ler arquivo do TSE").on("ano=" + ano + " eleitos=" + porSq.size()).info();
        return new ArrayList<>(porSq.values());
    }

    private List<CandidatoEleito> lerCsv(Path csv, int ano) {
        Map<String, CandidatoEleito> porSq = new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(csv)) {
            lerStream(in, ano, porSq);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + csv, e);
        }
        return new ArrayList<>(porSq.values());
    }

    static void lerStream(InputStream in, int ano, Map<String, CandidatoEleito> destino) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        CsvTse.ler(reader, r -> {
            String situacao = r.getOrDefault("DS_SIT_TOT_TURNO", "");
            if (!situacao.toUpperCase().startsWith("ELEITO")) return;
            String sq = r.get("SQ_CANDIDATO");
            if (sq == null || sq.isBlank()) return;
            // Quem foi ao 2º turno aparece duas vezes; mantemos a última linha (resultado final).
            destino.put(sq, new CandidatoEleito(
                    sq, ano,
                    r.get("NM_CANDIDATO"), r.get("NM_URNA_CANDIDATO"),
                    r.get("DS_CARGO"), r.get("SG_UF"), r.get("NM_UE"),
                    r.get("SG_PARTIDO"), r.get("NR_CANDIDATO"), situacao));
        });
    }
}
