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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Senado Federal — Dados Abertos (https://legis.senado.leg.br/dadosabertos).
 * <p>
 * O serviço antigo /senador/{codigo}/autorias foi descontinuado (desativação completa em
 * 01/02/2026) e substituído por /processo. A leitura do JSON é tolerante a nomes de campos,
 * e o nome do parâmetro de autoria fica em application.yml (radar.senado.parametro-autor)
 * para ser ajustado sem recompilar, caso o Senado o altere.
 * <p>
 * Coleta: /processo filtra por data de apresentação (até 1 ano por consulta, sem paginação) e
 * os autores com código de parlamentar só vêm no detalhe /processo/{id}.
 */
@Component
public class SenadoClient implements FonteComColeta {

    private static final Pattern IDENTIFICACAO = Pattern.compile("^\\s*([A-Za-z]+)\\s+(\\d+)\\s*/\\s*(\\d{4})");
    private static final String URL_MATERIA = "https://www25.senado.leg.br/web/atividade/materias/-/materia/";
    /** "Revisora" é projeto que veio da Câmara para revisão: já entra pela coleta da Câmara, com os autores de lá. */
    private static final String OBJETIVO_INICIADORA = "Iniciadora";
    /** A API devolve no máximo 1 ano por consulta; janelas maiores vão em pedaços. */
    private static final int MAX_DIAS_POR_CONSULTA = 365;

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
                porCodigo.putIfAbsent(codigo, senador(id));
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
            Proposicao proposicao = proposicao(p);
            if (ano != null && proposicao.ano() != null && !ano.equals(proposicao.ano())) continue;
            resultado.add(proposicao);
        }
        return resultado;
    }

    @Override
    public List<Proposicao> proposicoesApresentadasEntre(LocalDate de, LocalDate ate) {
        String tipos = String.join(",", cfg.tipos());
        List<Proposicao> resultado = new ArrayList<>();
        LocalDate inicio = de;
        while (!inicio.isAfter(ate)) {
            LocalDate limite = inicio.plusDays(MAX_DIAS_POR_CONSULTA - 1L);
            LocalDate fim = limite.isBefore(ate) ? limite : ate;
            final LocalDate i = inicio, f = fim;
            JsonNode resp = Json.get(http, codigo(), u -> u.path("/processo")
                    .queryParam("dataInicioApresentacao", i)
                    .queryParam("dataFimApresentacao", f)
                    .queryParam("sigla", tipos)
                    .build());
            JsonNode itens = Json.lista(resp, "processos", "dados", "itens");
            if (itens != null) {
                for (JsonNode p : itens) {
                    if (OBJETIVO_INICIADORA.equalsIgnoreCase(Json.texto(p, "objetivo"))) resultado.add(proposicao(p));
                }
            }
            inicio = fim.plusDays(1);
        }
        return resultado;
    }

    @Override
    public List<Autor> autoresDa(String idProposicao) {
        JsonNode resp = Json.get(http, codigo(), u -> u.path("/processo/{id}").build(idProposicao));
        JsonNode autoria = Json.lista(resp, "autoriaIniciativa");
        if (autoria == null) return List.of();
        List<Autor> autores = new ArrayList<>();
        for (JsonNode a : autoria) {
            // Só senadores são parlamentares desta casa; comissões, Presidência etc. ficam sem vínculo.
            String codigo = "SENADOR".equalsIgnoreCase(Json.texto(a, "siglaTipo")) ? Json.texto(a, "codigoParlamentar") : null;
            autores.add(new Autor(Json.texto(a, "autor"), Json.texto(a, "descricaoTipo", "siglaTipo"), codigo, true));
        }
        return autores;
    }

    @Override
    public Parlamentar parlamentar(String idParlamentar) {
        JsonNode resp = Json.get(http, codigo(), u -> u.path("/senador/{codigo}.json").build(idParlamentar));
        JsonNode id = resp.path("DetalheParlamentar").path("Parlamentar").path("IdentificacaoParlamentar");
        if (Json.texto(id, "CodigoParlamentar") == null) {
            throw new FonteIndisponivelException(codigo(), "senador " + idParlamentar + " sem IdentificacaoParlamentar", null);
        }
        return senador(id);
    }

    private Parlamentar senador(JsonNode id) {
        return new Parlamentar(codigo(), Json.texto(id, "CodigoParlamentar"), esfera(), nomeCasa(),
                Json.texto(id, "NomeParlamentar"), Json.texto(id, "SiglaPartidoParlamentar"),
                Json.texto(id, "UfParlamentar"), Json.texto(id, "UrlFotoParlamentar"));
    }

    private Proposicao proposicao(JsonNode p) {
        String identificacao = Json.texto(p, "identificacao", "descricaoIdentificacao");
        String sigla = Json.texto(p, "sigla", "siglaTipo");
        Integer numero = Json.inteiro(p, "numero");
        Integer ano = Json.inteiro(p, "ano");
        if (identificacao != null && (sigla == null || numero == null || ano == null)) {
            Matcher m = IDENTIFICACAO.matcher(identificacao);
            if (m.find()) {
                sigla = sigla != null ? sigla : m.group(1).toUpperCase();
                numero = numero != null ? numero : Integer.valueOf(m.group(2));
                ano = ano != null ? ano : Integer.valueOf(m.group(3));
            }
        }
        String codigoMateria = Json.texto(p, "codigoMateria");
        String id = Json.texto(p, "id", "idProcesso", "codigoMateria");
        String situacao = Json.texto(p, "situacao", "situacaoAtual", "descricaoSituacao");
        if (situacao == null) {
            String tramitando = Json.texto(p, "tramitando");
            if (tramitando != null) situacao = tramitando.equalsIgnoreCase("Sim") ? "Em tramitação" : "Encerrado";
        }
        return new Proposicao(codigo(), id, esfera(), nomeCasa(), sigla, numero, ano,
                Json.texto(p, "ementa"), Json.data(p, "dataApresentacao", "data"),
                situacao, codigoMateria != null ? URL_MATERIA + codigoMateria : null);
    }
}
