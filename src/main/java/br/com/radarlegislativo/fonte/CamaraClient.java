package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.config.RadarProperties;
import br.com.radarlegislativo.exception.FonteIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Câmara dos Deputados — API Dados Abertos v2 (https://dadosabertos.camara.leg.br/api/v2).
 * Sem autenticação; respostas em {"dados": [...], "links": [...]}, até 100 itens por página.
 */
@Component
public class CamaraClient implements FonteComColeta {

    private static final String URL_FICHA = "https://www.camara.leg.br/proposicoesWeb/fichadetramitacao?idProposicao=";
    private static final String PREFIXO_URI_DEPUTADO = "/deputados/";
    /** Trava de segurança: 100 páginas de 100 itens já é bem mais que um mês de PL/PEC/PLP/PDL. */
    private static final int MAX_PAGINAS_COLETA = 100;

    private final RestClient http;
    private final RadarProperties.Camara cfg;

    public CamaraClient(RestClient.Builder radarRestClientBuilder, RadarProperties props) {
        this.cfg = props.camara();
        this.http = radarRestClientBuilder.clone().baseUrl(cfg.baseUrl()).build();
    }

    @Override public String codigo() { return "camara"; }
    @Override public String nomeCasa() { return "Câmara dos Deputados"; }
    @Override public Esfera esfera() { return Esfera.CAMARA_FEDERAL; }

    @Override
    public List<Parlamentar> buscarParlamentares(String nome, String uf) {
        // Busca em cada legislatura configurada (a atual e a dos recém-eleitos) e remove repetidos.
        Map<String, Parlamentar> porId = new LinkedHashMap<>();
        for (Integer leg : cfg.legislaturas()) {
            JsonNode resp = Json.get(http, codigo(), u -> {
                UriBuilder b = u.path("/deputados")
                        .queryParam("idLegislatura", leg)
                        .queryParam("itens", 100)
                        .queryParam("ordem", "ASC")
                        .queryParam("ordenarPor", "nome");
                if (nome != null && !nome.isBlank()) b.queryParam("nome", nome);
                if (uf != null && !uf.isBlank()) b.queryParam("siglaUf", uf.toUpperCase());
                return b.build();
            });
            for (JsonNode d : resp.path("dados")) {
                porId.putIfAbsent(Json.texto(d, "id"), deputado(d));
            }
        }
        return new ArrayList<>(porId.values());
    }

    @Override
    public List<Proposicao> proposicoesDoAutor(String idParlamentar, Integer ano) {
        // Atenção: sem filtro de data a API devolve só os últimos 30 dias.
        // Por isso, sem ano informado, pedimos desde 2019 (início da 56ª legislatura).
        List<Proposicao> resultado = new ArrayList<>();
        for (int pagina = 1; pagina <= cfg.maxPaginas(); pagina++) {
            final int p = pagina;
            JsonNode resp = Json.get(http, codigo(), u -> {
                UriBuilder b = u.path("/proposicoes")
                        .queryParam("idDeputadoAutor", idParlamentar)
                        .queryParam("itens", 100)
                        .queryParam("pagina", p)
                        .queryParam("ordem", "DESC")
                        .queryParam("ordenarPor", "id");
                if (ano != null) {
                    b.queryParam("ano", ano);
                } else {
                    b.queryParam("dataApresentacaoInicio", "2019-02-01");
                }
                return b.build();
            });
            for (JsonNode d : resp.path("dados")) {
                resultado.add(proposicao(d));
            }
            if (!temProximaPagina(resp)) break;
        }
        return resultado;
    }

    @Override
    public List<Proposicao> proposicoesApresentadasEntre(LocalDate de, LocalDate ate) {
        // Sem filtro de tipo a Câmara devolve centenas de itens por dia (pareceres, emendas, requerimentos).
        String tipos = String.join(",", cfg.tipos());
        List<Proposicao> resultado = new ArrayList<>();
        for (int pagina = 1; ; pagina++) {
            if (pagina > MAX_PAGINAS_COLETA) {
                // Melhor falhar do que devolver a janela pela metade e a coleta dar o período por coberto.
                throw new FonteIndisponivelException(codigo(), "mais de " + MAX_PAGINAS_COLETA + " páginas entre " + de + " e " + ate, null);
            }
            final int p = pagina;
            JsonNode resp = Json.get(http, codigo(), u -> u.path("/proposicoes")
                    .queryParam("dataApresentacaoInicio", de)
                    .queryParam("dataApresentacaoFim", ate)
                    .queryParam("siglaTipo", tipos)
                    .queryParam("itens", 100)
                    .queryParam("pagina", p)
                    .queryParam("ordem", "ASC")
                    .queryParam("ordenarPor", "id")
                    .build());
            for (JsonNode d : resp.path("dados")) {
                resultado.add(proposicao(d));
            }
            if (!temProximaPagina(resp)) return resultado;
        }
    }

    @Override
    public List<Autor> autoresDa(String idProposicao) {
        JsonNode resp = Json.get(http, codigo(), u -> u.path("/proposicoes/{id}/autores").build(idProposicao));
        List<Autor> autores = new ArrayList<>();
        for (JsonNode a : resp.path("dados")) {
            // Só deputados têm uri em /deputados/{id}; Executivo, comissões e Senado apontam para outros recursos.
            String uri = Json.texto(a, "uri");
            String idDeputado = uri != null && uri.contains(PREFIXO_URI_DEPUTADO)
                    ? uri.substring(uri.lastIndexOf('/') + 1)
                    : null;
            autores.add(new Autor(Json.texto(a, "nome"), Json.texto(a, "tipo"), idDeputado,
                    "1".equals(Json.texto(a, "proponente"))));
        }
        return autores;
    }

    @Override
    public Parlamentar parlamentar(String idParlamentar) {
        JsonNode resp = Json.get(http, codigo(), u -> u.path("/deputados/{id}").build(idParlamentar));
        JsonNode status = resp.path("dados").path("ultimoStatus");
        if (Json.texto(status, "id") == null) {
            throw new FonteIndisponivelException(codigo(), "deputado " + idParlamentar + " sem ultimoStatus", null);
        }
        return deputado(status);
    }

    private Parlamentar deputado(JsonNode d) {
        return new Parlamentar(codigo(), Json.texto(d, "id"), esfera(), nomeCasa(),
                Json.texto(d, "nome"), Json.texto(d, "siglaPartido"),
                Json.texto(d, "siglaUf"), Json.texto(d, "urlFoto"));
    }

    private Proposicao proposicao(JsonNode d) {
        String id = Json.texto(d, "id");
        return new Proposicao(codigo(), id, esfera(), nomeCasa(),
                Json.texto(d, "siglaTipo"), Json.inteiro(d, "numero"), Json.inteiro(d, "ano"),
                Json.texto(d, "ementa"), Json.data(d, "dataApresentacao"),
                null, URL_FICHA + id);
    }

    private boolean temProximaPagina(JsonNode resp) {
        for (JsonNode link : resp.path("links")) {
            if ("next".equals(Json.texto(link, "rel"))) return true;
        }
        return false;
    }
}
