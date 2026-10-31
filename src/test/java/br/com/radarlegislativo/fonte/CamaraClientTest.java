package br.com.radarlegislativo.fonte;

import br.com.radarlegislativo.exception.FonteIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static br.com.radarlegislativo.TestFixtures.props;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CamaraClientTest {

    private MockRestServiceServer server;
    private CamaraClient camara;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        camara = new CamaraClient(builder, props("x"));
    }

    @Test
    @DisplayName("deve mapear deputados da resposta da API")
    void deveMapearDeputados() {
        server.expect(requestTo(startsWith("https://camara.test/api/v2/deputados?idLegislatura=57")))
                .andExpect(queryParam("nome", "Amaral"))
                .andRespond(withSuccess("""
                        {"dados":[{"id":204534,"nome":"Tabata Amaral","siglaPartido":"PSB","siglaUf":"SP",
                        "idLegislatura":57,"urlFoto":"https://www.camara.leg.br/internet/deputado/bandep/204534.jpg"}],"links":[]}
                        """, MediaType.APPLICATION_JSON));

        List<Parlamentar> deputados = camara.buscarParlamentares("Amaral", null);

        assertThat(deputados).singleElement().satisfies(p -> {
            assertThat(p.id()).isEqualTo("204534");
            assertThat(p.partido()).isEqualTo("PSB");
        });
        server.verify();
    }

    @Test
    @DisplayName("deve seguir a paginação de proposições enquanto houver link next")
    void deveSeguirPaginacaoDeProposicoes() {
        server.expect(requestTo(startsWith("https://camara.test/api/v2/proposicoes?idDeputadoAutor=204534")))
                .andExpect(queryParam("pagina", "1"))
                .andRespond(withSuccess("""
                        {"dados":[{"id":2500001,"siglaTipo":"PL","numero":1234,"ano":2025,"ementa":"Dispõe sobre X."}],
                         "links":[{"rel":"next","href":"..."}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith("https://camara.test/api/v2/proposicoes?idDeputadoAutor=204534")))
                .andExpect(queryParam("pagina", "2"))
                .andRespond(withSuccess("""
                        {"dados":[{"id":2400002,"siglaTipo":"PEC","numero":7,"ano":2024,"ementa":"Altera a Constituição."}],"links":[]}
                        """, MediaType.APPLICATION_JSON));

        List<Proposicao> proposicoes = camara.proposicoesDoAutor("204534", null);

        assertThat(proposicoes).extracting(Proposicao::identificacao).containsExactly("PL 1234/2025", "PEC 7/2024");
        assertThat(proposicoes.getFirst().url()).endsWith("idProposicao=2500001");
        server.verify();
    }

    @Test
    @DisplayName("deve lançar FonteIndisponivel quando a API responde erro")
    void deveLancarFonteIndisponivelQuandoApiFalha() {
        server.expect(requestTo(startsWith("https://camara.test/api/v2/deputados")))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> camara.buscarParlamentares("Amaral", null))
                .isInstanceOf(FonteIndisponivelException.class)
                .satisfies(e -> assertThat(((FonteIndisponivelException) e).fonte()).isEqualTo("camara"));
    }

    @Test
    @DisplayName("deve coletar por período e tipo, seguindo a paginação")
    void deveColetarPorPeriodoETipo() {
        server.expect(requestTo(startsWith("https://camara.test/api/v2/proposicoes?dataApresentacaoInicio=2026-10-29")))
                .andExpect(queryParam("dataApresentacaoFim", "2026-10-30"))
                .andExpect(queryParam("siglaTipo", "PL,PEC"))
                .andExpect(queryParam("pagina", "1"))
                .andRespond(withSuccess("""
                        {"dados":[{"id":2648825,"siglaTipo":"PL","numero":5660,"ano":2026,"ementa":"Altera o CDC.",
                         "dataApresentacao":"2026-10-29T09:54"}],
                         "links":[{"rel":"next","href":"..."}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith("https://camara.test/api/v2/proposicoes?dataApresentacaoInicio=2026-10-29")))
                .andExpect(queryParam("pagina", "2"))
                .andRespond(withSuccess("""
                        {"dados":[{"id":2648900,"siglaTipo":"PEC","numero":12,"ano":2026,"ementa":"Altera a Constituição.",
                         "dataApresentacao":"2026-10-30T10:00"}],"links":[]}
                        """, MediaType.APPLICATION_JSON));

        List<Proposicao> proposicoes = camara.proposicoesApresentadasEntre(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 10, 30));

        assertThat(proposicoes).extracting(Proposicao::identificacao).containsExactly("PL 5660/2026", "PEC 12/2026");
        assertThat(proposicoes.getFirst().dataApresentacao()).isEqualTo(LocalDate.of(2026, 10, 29));
        server.verify();
    }

    @Test
    @DisplayName("deve ligar ao deputado só os autores que são deputados")
    void deveMapearAutores() {
        server.expect(requestTo("https://camara.test/api/v2/proposicoes/2648825/autores"))
                .andRespond(withSuccess("""
                        {"dados":[
                          {"uri":"https://dadosabertos.camara.leg.br/api/v2/deputados/220599","nome":"Marcelo Crivella",
                           "codTipo":10000,"tipo":"Deputado(a)","ordemAssinatura":1,"proponente":1},
                          {"uri":"https://dadosabertos.camara.leg.br/api/v2/orgaos/78","nome":"Senado Federal",
                           "codTipo":40000,"tipo":"Órgão do Poder Legislativo","ordemAssinatura":2,"proponente":0}
                        ],"links":[]}
                        """, MediaType.APPLICATION_JSON));

        List<Autor> autores = camara.autoresDa("2648825");

        assertThat(autores).containsExactly(
                new Autor("Marcelo Crivella", "Deputado(a)", "220599", true),
                new Autor("Senado Federal", "Órgão do Poder Legislativo", null, false));
    }

    @Test
    @DisplayName("deve ler o deputado do ultimoStatus")
    void deveLerDeputadoDoUltimoStatus() {
        server.expect(requestTo("https://camara.test/api/v2/deputados/220599"))
                .andRespond(withSuccess("""
                        {"dados":{"id":220599,"nomeCivil":"MARCELO BEZERRA CRIVELLA",
                          "ultimoStatus":{"id":220599,"nome":"Marcelo Crivella","siglaPartido":"REPUBLICANOS","siglaUf":"RJ",
                          "urlFoto":"https://www.camara.leg.br/internet/deputado/bandep/220599.jpg"}}}
                        """, MediaType.APPLICATION_JSON));

        Parlamentar p = camara.parlamentar("220599");

        assertThat(p.id()).isEqualTo("220599");
        assertThat(p.nome()).isEqualTo("Marcelo Crivella");
        assertThat(p.partido()).isEqualTo("REPUBLICANOS");
        assertThat(p.uf()).isEqualTo("RJ");
    }
}
