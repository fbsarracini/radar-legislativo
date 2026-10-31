package br.com.radarlegislativo.config;

import br.com.radarlegislativo.log.AppLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;

/**
 * As APIs públicas oscilam (a Câmara devolve 504 "upstream request timeout" no meio de coletas longas).
 * Medido em 31/10/2026: 25–50% dos requests à Câmara voltam 504 após 15s, de forma independente entre si —
 * por isso muitas tentativas com espera curta, e não poucas com espera longa.
 * Repete GET/HEAD em 5xx, 429 e falha de rede/timeout, com espera dobrando a cada tentativa até um teto.
 * 4xx não se repete: o pedido está errado e vai continuar errado.
 */
public class RetentativaInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RetentativaInterceptor.class);

    /** Injetável para os testes não dormirem de verdade. */
    @FunctionalInterface
    interface Espera {
        void esperar(Duration duracao) throws InterruptedException;
    }

    private final int tentativas;
    private final Duration esperaInicial;
    private final Duration esperaMaxima;
    private final Espera espera;

    public RetentativaInterceptor(int tentativas, Duration esperaInicial, Duration esperaMaxima) {
        this(tentativas, esperaInicial, esperaMaxima, d -> Thread.sleep(d.toMillis()));
    }

    RetentativaInterceptor(int tentativas, Duration esperaInicial, Duration esperaMaxima, Espera espera) {
        Assert.isTrue(tentativas >= 1, "deve haver ao menos uma tentativa");
        Assert.isTrue(esperaMaxima.compareTo(esperaInicial) >= 0, "a espera máxima não pode ser menor que a inicial");
        this.tentativas = tentativas;
        this.esperaInicial = esperaInicial;
        this.esperaMaxima = esperaMaxima;
        this.espera = espera;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        boolean idempotente = request.getMethod() == HttpMethod.GET || request.getMethod() == HttpMethod.HEAD;
        Duration proxima = esperaInicial;
        for (int tentativa = 1; ; tentativa++) {
            boolean ultima = !idempotente || tentativa >= tentativas;
            String motivo;
            try {
                ClientHttpResponse response = execution.execute(request, body);
                if (ultima || !transitorio(response)) return response;
                motivo = "HTTP " + response.getStatusCode().value();
                response.close();
            } catch (InterruptedIOException e) {
                // Timeout de leitura é InterruptedIOException, mas não é a thread sendo interrompida.
                if (ultima || Thread.currentThread().isInterrupted()) throw e;
                motivo = e.getClass().getSimpleName();
            } catch (IOException e) {
                if (ultima) throw e;
                motivo = e.getClass().getSimpleName();
            }
            AppLog.logger(log).system().does("repetir request")
                    .on(request.getURI().getHost() + request.getURI().getPath() + " tentativa=" + tentativa + "/" + tentativas)
                    .failed(motivo).warn();
            try {
                espera.esperar(proxima);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException("interrompido esperando nova tentativa");
            }
            Duration dobro = proxima.multipliedBy(2);
            proxima = dobro.compareTo(esperaMaxima) > 0 ? esperaMaxima : dobro;
        }
    }

    private static boolean transitorio(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is5xxServerError()
                || response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }
}
