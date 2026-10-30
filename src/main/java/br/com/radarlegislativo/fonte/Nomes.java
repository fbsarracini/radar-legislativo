package br.com.radarlegislativo.fonte;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/** Normalização de nomes para cruzar TSE x casas legislativas ("José da Silva" == "JOSE DA SILVA"). */
public final class Nomes {

    private static final Set<String> PARTICULAS = Set.of("DA", "DE", "DO", "DAS", "DOS", "E");
    private static final Set<String> PREFIXOS = Set.of("DEPUTADO", "DEPUTADA", "SENADOR", "SENADORA",
            "VEREADOR", "VEREADORA", "DR", "DRA", "PROF", "PROFESSOR", "PROFESSORA");

    private Nomes() {}

    /** Maiúsculas, sem acentos, sem pontuação, espaços simples. Nulo vira "". */
    public static String normalizar(String nome) {
        if (nome == null) return "";
        String s = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    /** Versão para comparação "frouxa": também remove títulos (Dr., Senador...) e partículas (da, de...). */
    public static String chave(String nome) {
        StringBuilder sb = new StringBuilder();
        for (String parte : normalizar(nome).split(" ")) {
            if (parte.isEmpty() || PARTICULAS.contains(parte) || PREFIXOS.contains(parte)) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(parte);
        }
        return sb.toString();
    }

    /** Todas as palavras de {@code curto} aparecem em {@code longo}. */
    public static boolean contidoEm(String curto, String longo) {
        String c = chave(curto), l = " " + chave(longo) + " ";
        if (c.isEmpty()) return false;
        for (String parte : c.split(" ")) {
            if (!l.contains(" " + parte + " ")) return false;
        }
        return true;
    }
}
