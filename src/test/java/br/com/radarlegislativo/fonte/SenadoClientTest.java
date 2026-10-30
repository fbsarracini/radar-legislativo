package br.com.radarlegislativo.fonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.props;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SenadoClientTest {

    private MockRestServiceServer server;
    private SenadoClient senado;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        senado = new SenadoClient(builder, props("x"));
    }

    @Test
    @DisplayName("deve filtrar senadores por nome e UF localmente")
    void deveFiltrarSenadoresPorNomeEUf() {
        server.expect(requestTo("https://senado.test/dadosabertos/senador/lista/atual.json"))
                .andRespond(withSuccess("""
                        {"ListaParlamentarEmExercicio":{"Parlamentares":{"Parlamentar":[
                          {"IdentificacaoParlamentar":{"CodigoParlamentar":"5322","NomeParlamentar":"Romário",
                           "SiglaPartidoParlamentar":"PL","UfParlamentar":"RJ"}},
                          {"IdentificacaoParlamentar":{"CodigoParlamentar":"9999","NomeParlamentar":"Outro Nome",
                           "SiglaPartidoParlamentar":"PT","UfParlamentar":"BA"}}]}}}
                        """, MediaType.APPLICATION_JSON));

        List<Parlamentar> senadores = senado.buscarParlamentares("romario", "RJ");

        assertThat(senadores).extracting(Parlamentar::id).containsExactly("5322");
        server.verify();
    }

    @Test
    @DisplayName("deve extrair sigla, número e ano do campo identificacao")
    void deveExtrairIdentificacaoDoProcesso() {
        server.expect(requestTo("https://senado.test/dadosabertos/processo?codigoParlamentarAutor=5322"))
                .andRespond(withSuccess("""
                        [{"id":8631287,"codigoMateria":162500,"identificacao":"RQS 158/2024",
                          "ementa":"Requer criação de CPI.","dataApresentacao":"2024-04-10","tramitando":"Não"}]
                        """, MediaType.APPLICATION_JSON));

        List<Proposicao> proposicoes = senado.proposicoesDoAutor("5322", null);

        assertThat(proposicoes).singleElement().satisfies(p -> {
            assertThat(p.identificacao()).isEqualTo("RQS 158/2024");
            assertThat(p.situacao()).isEqualTo("Encerrado");
            assertThat(p.url()).endsWith("/materia/162500");
        });
        server.verify();
    }
}
