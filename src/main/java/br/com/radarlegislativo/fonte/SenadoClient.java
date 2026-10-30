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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Senado Federal — Dados Abertos (https://legis.senado.leg.br/dadosabertos).
 * <p>
 * O serviço antigo /senador/{codigo}/autorias foi descontinuado (desativação completa em
 * 01/02/2026) e substituído por /processo. A leitura do JSON é tolerante a nomes de campos,
 * e o nome do parâmetro de autoria fica em application.yml (radar.senado.parametro-autor)
 * para ser ajustado sem recompilar, caso o Senado o altere.
 */
@Component
public class SenadoClient implements FonteLegislativa {

    private static final Pattern IDENTIFICACAO = Pattern.compile("^\\s*([A-Za-z]+)\\s+(\\d+)\\s*/\\s*(\\d{4})");
    private static final String URL_MATERIA = "https://www25.senado.leg.br/web/atividade/materias/-/materia/";

    private final RestClient http;
    private final RadarProperties.Senado cfg;

    public SenadoClient(RestClient.Builder radarRestClientBuilder, RadarProperties props) {
        this.cfg = props.senado();
        this.http = radarRestClientBuilder.clone().baseUrl(cfg.baseUrl()).build();
    }

    @Override public String codigo() { return "senado"; }
    @Override public String nomeCasa() { return "Senado Federal"; }
    @Override public Esfera esfera() { return Esfera.SENADO_FEDERAL; }

    @Override
    public List<Parlamentar> buscarParlamentares(String nome, String uf) {
        // A API do Senado não filtra por nome: baixamos as listas (pequenas, ~81 por legislatura) e filtramos aqui.
        Map<String, Parlamentar> porCodigo = new LinkedHashMap<>();
        List<String> caminhos = new ArrayList<>();
        caminhos.add("/senador/lista/atual.json");
        for (Integer leg : cfg.legislaturas()) caminhos.add("/senador/lista/legislatura/" + leg + ".json");

        String alvo = Nomes.normalizar(nome);
        for (String caminho : caminhos) {
            JsonNode resp = Json.get(http, codigo(), u -> u.path(caminho).build());
            for (JsonNode id : Json.todos(resp, "IdentificacaoParlamentar")) {
                String codigo = Json.texto(id, "CodigoParlamentar");
                String nomeParl = Json.texto(id, "NomeParlamentar");
                String ufParl = Json.texto(id, "UfParlamentar");
                if (codigo == null) continue;
                if (!alvo.isEmpty()
                        && !Nomes.normalizar(nomeParl).contains(alvo)
                        && !Nomes.normalizar(Json.texto(id, "NomeCompletoParlamentar")).contains(alvo)) continue;
                if (uf != null && !uf.isBlank() && !uf.equalsIgnoreCase(ufParl)) continue;
                porCodigo.putIfAbsent(codigo, new Parlamentar(codigo(), codigo, esfera(), nomeCasa(), nomeParl,
                        Json.texto(id, "SiglaPartidoParlamentar"), ufParl, Json.texto(id, "UrlFotoParlamentar")));
            }
        }
        return new ArrayList<>(porCodigo.values());
    }

    @Override
    public List<Proposicao> proposicoesDoAutor(String idParlamentar, Integer ano) {
        JsonNode resp = Json.get(http, codigo(), u -> {
            UriBuilder b = u.path("/processo").queryParam(cfg.parametroAutor(), idParlamentar);
            if (ano != null) b.queryParam("ano", ano);
            return b.build();
        });

        JsonNode itens = Json.lista(resp, "processos", "dados", "itens");
        if (itens == null) return List.of();

        List<Proposicao> resultado = new ArrayList<>();
        for (JsonNode p : itens) {
            String identificacao = Json.texto(p, "identificacao", "descricaoIdentificacao");
            String sigla = Json.texto(p, "sigla", "siglaTipo");
            Integer numero = Json.inteiro(p, "numero");
            Integer anoProc = Json.inteiro(p, "ano");
            if (identificacao != null && (sigla == null || numero == null || anoProc == null)) {
                Matcher m = IDENTIFICACAO.matcher(identificacao);
                if (m.find()) {
                    sigla = sigla != null ? sigla : m.group(1).toUpperCase();
                    numero = numero != null ? numero : Integer.valueOf(m.group(2));
                    anoProc = anoProc != null ? anoProc : Integer.valueOf(m.group(3));
                }
            }
            if (ano != null && anoProc != null && !ano.equals(anoProc)) continue;

            String codigoMateria = Json.texto(p, "codigoMateria");
            String id = Json.texto(p, "id", "idProcesso", "codigoMateria");
            String situacao = Json.texto(p, "situacao", "descricaoSituacao");
            if (situacao == null) {
                String tramitando = Json.texto(p, "tramitando");
                if (tramitando != null) situacao = tramitando.equalsIgnoreCase("Sim") ? "Em tramitação" : "Encerrado";
            }
            resultado.add(new Proposicao(codigo(), id, esfera(), nomeCasa(), sigla, numero, anoProc,
                    Json.texto(p, "ementa"), Json.data(p, "dataApresentacao", "data"),
                    situacao, codigoMateria != null ? URL_MATERIA + codigoMateria : null));
        }
        return resultado;
    }
}
