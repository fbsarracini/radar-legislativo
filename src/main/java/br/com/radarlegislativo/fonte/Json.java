package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.exception.FonteIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Leitura tolerante de JSON: as APIs públicas mudam nomes de campos com frequência. */
final class Json {

    private Json() {}

    /** GET que devolve o corpo como JSON; qualquer falha vira {@link FonteIndisponivelException}. */
    static JsonNode get(RestClient http, String fonte, Function<UriBuilder, URI> uri) {
        try {
            JsonNode body = http.get().uri(uri).retrieve().body(JsonNode.class);
            if (body == null) throw new FonteIndisponivelException(fonte, "resposta vazia", null);
            return body;
        } catch (RestClientException e) {
            throw new FonteIndisponivelException(fonte, e.getMessage(), e);
        }
    }

    /** Primeiro campo textual não vazio dentre os nomes informados. */
    static String texto(JsonNode no, String... campos) {
        if (no == null) return null;
        for (String campo : campos) {
            JsonNode v = no.get(campo);
            if (v != null && !v.isNull() && !v.asText().isBlank()) {
                return v.asText().trim();
            }
        }
        return null;
    }

    static Integer inteiro(JsonNode no, String... campos) {
        String t = texto(no, campos);
        if (t == null) return null;
        try {
            return Integer.parseInt(t.replaceFirst("^0+(?=\\d)", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static LocalDate data(JsonNode no, String... campos) {
        String t = texto(no, campos);
        if (t == null || t.length() < 10) return null;
        try {
            return LocalDate.parse(t.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Coleta, em qualquer profundidade, todos os nós com o nome dado. */
    static List<JsonNode> todos(JsonNode raiz, String nome) {
        List<JsonNode> achados = new ArrayList<>();
        coletar(raiz, nome, achados);
        return achados;
    }

    private static void coletar(JsonNode no, String nome, List<JsonNode> achados) {
        if (no == null) return;
        if (no.isObject()) {
            no.fields().forEachRemaining(e -> {
                if (e.getKey().equals(nome)) {
                    if (e.getValue().isArray()) e.getValue().forEach(achados::add);
                    else achados.add(e.getValue());
                } else {
                    coletar(e.getValue(), nome, achados);
                }
            });
        } else if (no.isArray()) {
            no.forEach(filho -> coletar(filho, nome, achados));
        }
    }

    /** Devolve o próprio nó se for array, ou o primeiro array encontrado nos campos dados. */
    static JsonNode lista(JsonNode raiz, String... campos) {
        if (raiz == null) return null;
        if (raiz.isArray()) return raiz;
        for (String c : campos) {
            JsonNode v = raiz.get(c);
            if (v != null && v.isArray()) return v;
        }
        return null;
    }
}
