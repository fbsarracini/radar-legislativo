package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.config.RadarProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Casa legislativa que usa o SAPL (Sistema de Apoio ao Processo Legislativo, do Interlegis).
 * É o caminho mais viável para as esferas estadual e municipal: não existe API nacional
 * que reúna assembleias e câmaras municipais, mas centenas delas rodam SAPL com a mesma API.
 * <p>
 * Uma instância por casa, criada a partir de radar.sapl em application.yml.
 * Os filtros da API do SAPL variam entre versões, por isso filtramos também do nosso lado.
 */
public class SaplClient implements FonteLegislativa {

    private static final int MAX_PAGINAS = 20;

    private final RestClient http;
    private final RadarProperties.CasaSapl casa;

    public SaplClient(RestClient.Builder builder, RadarProperties.CasaSapl casa) {
        this.casa = casa;
        this.http = builder.clone().baseUrl(casa.baseUrl()).build();
    }

    @Override public String codigo() { return "sapl-" + casa.slug(); }
    @Override public String nomeCasa() { return casa.nome(); }
    @Override public Esfera esfera() { return casa.esfera(); }
    @Override public String uf() { return casa.uf(); }
    @Override public String municipio() { return casa.municipio(); }

    @Override
    public List<Parlamentar> buscarParlamentares(String nome, String uf) {
        if (uf != null && !uf.isBlank() && casa.uf() != null && !uf.equalsIgnoreCase(casa.uf())) {
            return List.of();
        }
        String alvo = Nomes.normalizar(nome);
        List<Parlamentar> resultado = new ArrayList<>();
        paginar(u -> {
            UriBuilder b = u.path("/api/base/autor/");
            if (nome != null && !nome.isBlank()) b.queryParam("nome", nome);
            return b;
        }, a -> {
            String nomeAutor = Json.texto(a, "nome", "__str__");
            if (!alvo.isEmpty() && !Nomes.normalizar(nomeAutor).contains(alvo)) return;
            resultado.add(new Parlamentar(codigo(), Json.texto(a, "id"), esfera(), nomeCasa(),
                    nomeAutor, null, casa.uf(), null));
        });
        return resultado;
    }

    @Override
    public List<Proposicao> proposicoesDoAutor(String idParlamentar, Integer ano) {
        List<Proposicao> resultado = new ArrayList<>();
        paginar(u -> {
            UriBuilder b = u.path("/api/materia/materialegislativa/").queryParam("autores", idParlamentar);
            if (ano != null) b.queryParam("ano", ano);
            return b;
        }, m -> {
            Integer anoMat = Json.inteiro(m, "ano");
            if (ano != null && anoMat != null && !ano.equals(anoMat)) return;
            String id = Json.texto(m, "id");
            String descricao = Json.texto(m, "__str__");
            String sigla = descricao != null && descricao.contains(" ") ? descricao.substring(0, descricao.indexOf(' ')) : null;
            resultado.add(new Proposicao(codigo(), id, esfera(), nomeCasa(),
                    sigla, Json.inteiro(m, "numero"), anoMat, Json.texto(m, "ementa"),
                    Json.data(m, "data_apresentacao"),
                    m.path("em_tramitacao").asBoolean(false) ? "Em tramitação" : null,
                    casa.baseUrl().replaceAll("/+$", "") + "/materia/" + id));
        });
        return resultado;
    }

    private void paginar(Function<UriBuilder, UriBuilder> consulta, Consumer<JsonNode> porItem) {
        for (int pagina = 1; pagina <= MAX_PAGINAS; pagina++) {
            final int p = pagina;
            JsonNode resp = Json.get(http, codigo(), u -> consulta.apply(u).queryParam("page", p).build());
            JsonNode itens = Json.lista(resp, "results");
            if (itens == null) return;
            itens.forEach(porItem);
            JsonNode pag = resp.path("pagination");
            boolean temProxima = pag.hasNonNull("next_page") || resp.hasNonNull("next");
            if (!temProxima) return;
        }
    }
}
