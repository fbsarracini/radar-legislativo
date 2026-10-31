package br.com.radarlegislativo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RetentativaInterceptorTest {

    private static final String URL = "https://casa.test/api/x";

    private final List<Duration> esperas = new ArrayList<>();
    private MockRestServiceServer server;
    private RestClient http;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .requestInterceptor(new RetentativaInterceptor(3, Duration.ofSeconds(2), Duration.ofSeconds(3), esperas::add));
        server = MockRestServiceServer.bindTo(builder).build();
        http = builder.build();
    }

    private String get() {
        return http.get().uri(URL).retrieve().body(String.class);
    }

    @Test
    @DisplayName("deve repetir 504 e 429 com espera dobrando, até o teto, até dar certo")
    void deveRepetirTransitorios() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(requestTo(URL)).andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        assertThat(get()).isEqualTo("ok");
        assertThat(esperas).containsExactly(Duration.ofSeconds(2), Duration.ofSeconds(3));
        server.verify();
    }

    @Test
    @DisplayName("deve repetir timeout de rede")
    void deveRepetirTimeout() {
        server.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("read timed out")));
        server.expect(requestTo(URL)).andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        assertThat(get()).isEqualTo("ok");
        server.verify();
    }

    @Test
    @DisplayName("deve desistir na última tentativa e devolver o erro da casa")
    void deveDesistirNaUltimaTentativa() {
        server.expect(ExpectedCount.times(3), requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(this::get).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
        assertThat(esperas).hasSize(2);
        server.verify();
    }

    @Test
    @DisplayName("deve desistir de timeout persistente")
    void deveDesistirDeTimeoutPersistente() {
        server.expect(ExpectedCount.times(3), requestTo(URL)).andRespond(withException(new SocketTimeoutException("read timed out")));

        assertThatThrownBy(this::get).isInstanceOf(ResourceAccessException.class);
        server.verify();
    }

    @Test
    @DisplayName("não deve repetir 4xx")
    void naoDeveRepetir4xx() {
        server.expect(ExpectedCount.once(), requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(this::get).isInstanceOf(HttpClientErrorException.NotFound.class);
        assertThat(esperas).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("não deve repetir método que não é idempotente")
    void naoDeveRepetirPost() {
        server.expect(ExpectedCount.once(), requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> http.post().uri(URL).retrieve().toBodilessEntity())
                .isInstanceOf(HttpServerErrorException.BadGateway.class);
        assertThat(esperas).isEmpty();
    }
}
