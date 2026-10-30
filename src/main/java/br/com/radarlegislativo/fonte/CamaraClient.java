package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.config.RadarProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Câmara dos Deputados — API Dados Abertos v2 (https://dadosabertos.camara.leg.br/api/v2).
 * Sem autenticação; respostas em {"dados": [...], "links": [...]}, até 100 itens por página.
 */
@Component
public class CamaraClient implements FonteLegislativa {

    private static final String URL_FICHA = "https://www.camara.leg.br/proposicoesWeb/fichadetramitacao?idProposicao=";

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
                String id = Json.texto(d, "id");
                porId.putIfAbsent(id, new Parlamentar(codigo(), id, esfera(), nomeCasa(),
                        Json.texto(d, "nome"), Json.texto(d, "siglaPartido"),
                        Json.texto(d, "siglaUf"), Json.texto(d, "urlFoto")));
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
                String id = Json.texto(d, "id");
                resultado.add(new Proposicao(codigo(), id, esfera(), nomeCasa(),
                        Json.texto(d, "siglaTipo"), Json.inteiro(d, "numero"), Json.inteiro(d, "ano"),
                        Json.texto(d, "ementa"), Json.data(d, "dataApresentacao"),
                        null, URL_FICHA + id));
            }
            if (!temProximaPagina(resp)) break;
        }
        return resultado;
    }

    private boolean temProximaPagina(JsonNode resp) {
        for (JsonNode link : resp.path("links")) {
            if ("next".equals(Json.texto(link, "rel"))) return true;
        }
        return false;
    }
}
