package br.com.radarlegislativo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class HttpConfig {

    /** Builder compartilhado; cada fonte clona e define a própria baseUrl. */
    @Bean
    public RestClient.Builder radarRestClientBuilder(RadarProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(props.http().connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(props.http().readTimeout());

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, props.http().userAgent());
    }
}
