package br.com.radarlegislativo.eleito;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Leitor dos CSVs do TSE: separador ';', campos entre aspas, codificação Latin-1
 * (a codificação é responsabilidade de quem cria o Reader).
 */
public final class CsvTse {

    private CsvTse() {}

    /** Lê o cabeçalho e entrega cada linha como mapa coluna → valor. */
    public static void ler(BufferedReader reader, Consumer<Map<String, String>> porLinha) throws IOException {
        String linha = reader.readLine();
        if (linha == null) return;
        if (!linha.isEmpty() && linha.charAt(0) == '﻿') linha = linha.substring(1);
        List<String> cabecalho = dividir(linha);
        while ((linha = reader.readLine()) != null) {
            if (linha.isBlank()) continue;
            List<String> valores = dividir(linha);
            Map<String, String> registro = new HashMap<>(cabecalho.size() * 2);
            for (int i = 0; i < cabecalho.size() && i < valores.size(); i++) {
                registro.put(cabecalho.get(i), valores.get(i));
            }
            porLinha.accept(registro);
        }
    }

    static List<String> dividir(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (entreAspas) {
                if (c == '"') {
                    if (i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                        atual.append('"');
                        i++;
                    } else {
                        entreAspas = false;
                    }
                } else {
                    atual.append(c);
                }
            } else if (c == '"') {
                entreAspas = true;
            } else if (c == ';') {
                campos.add(atual.toString().trim());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString().trim());
        return campos;
    }
}
