package br.com.radarlegislativo.fonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import static br.com.radarlegislativo.TestFixtures.props;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
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

    @Test
    @DisplayName("deve coletar por período e tipo, só o que o Senado iniciou")
    void deveColetarSoIniciadoras() {
        server.expect(requestTo("https://senado.test/dadosabertos/processo?dataInicioApresentacao=2026-10-01&dataFimApresentacao=2026-10-31&sigla=PL,PEC"))
                .andRespond(withSuccess("""
                        [{"id":9111471,"codigoMateria":175895,"identificacao":"PL 5503/2026","objetivo":"Iniciadora",
                          "dataApresentacao":"2026-10-01","ementa":"Altera a Lei Orgânica da Saúde.","situacaoAtual":"AGUARDANDO DESPACHO",
                          "autoria":"Senadora Dra. Eudócia (PSDB/AL)"},
                         {"id":9113381,"codigoMateria":176000,"identificacao":"PL 5555/2026","objetivo":"Revisora",
                          "dataApresentacao":"2026-10-13","ementa":"Veio da Câmara.","autoria":"Câmara dos Deputados"}]
                        """, MediaType.APPLICATION_JSON));

        List<Proposicao> proposicoes = senado.proposicoesApresentadasEntre(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertThat(proposicoes).singleElement().satisfies(p -> {
            assertThat(p.id()).isEqualTo("9111471");
            assertThat(p.identificacao()).isEqualTo("PL 5503/2026");
            assertThat(p.dataApresentacao()).isEqualTo(LocalDate.of(2026, 10, 1));
            assertThat(p.situacao()).isEqualTo("AGUARDANDO DESPACHO");
            assertThat(p.url()).endsWith("/materia/175895");
        });
        server.verify();
    }

    @Test
    @DisplayName("deve quebrar janelas maiores que 1 ano, o limite da API")
    void deveQuebrarJanelaMaiorQueUmAno() {
        server.expect(requestTo(startsWith("https://senado.test/dadosabertos/processo?dataInicioApresentacao=2025-01-01&dataFimApresentacao=2025-12-31")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith("https://senado.test/dadosabertos/processo?dataInicioApresentacao=2026-01-01&dataFimApresentacao=2026-03-31")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(senado.proposicoesApresentadasEntre(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 3, 31))).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("deve ligar ao senador só os autores que são senadores")
    void deveMapearAutores() {
        server.expect(requestTo("https://senado.test/dadosabertos/processo/9111471"))
                .andRespond(withSuccess("""
                        {"id":9111471,"autoriaIniciativa":[
                          {"autor":"Dra. Eudócia","siglaTipo":"SENADOR","descricaoTipo":"SENADOR","ordem":1,"codigoParlamentar":5906,
                           "uf":"AL","siglaPartido":"PSDB","casaEnte":"SF"},
                          {"autor":"Comissão de Assuntos Sociais","siglaTipo":"COMISSAO","descricaoTipo":"COMISSÃO","ordem":2,"casaEnte":"SF"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(senado.autoresDa("9111471")).containsExactly(
                new Autor("Dra. Eudócia", "SENADOR", "5906", true),
                new Autor("Comissão de Assuntos Sociais", "COMISSÃO", null, true));
    }

    @Test
    @DisplayName("deve ler o senador do detalhe")
    void deveLerSenador() {
        server.expect(requestTo("https://senado.test/dadosabertos/senador/5906.json"))
                .andRespond(withSuccess("""
                        {"DetalheParlamentar":{"Parlamentar":{"IdentificacaoParlamentar":{"CodigoParlamentar":"5906",
                          "NomeParlamentar":"Dra. Eudócia","SiglaPartidoParlamentar":"PSDB","UfParlamentar":"AL",
                          "UrlFotoParlamentar":"http://www.senado.leg.br/senadores/img/fotos-oficiais/senador5906.jpg"}}}}
                        """, MediaType.APPLICATION_JSON));

        Parlamentar p = senado.parlamentar("5906");

        assertThat(p.nome()).isEqualTo("Dra. Eudócia");
        assertThat(p.partido()).isEqualTo("PSDB");
        assertThat(p.uf()).isEqualTo("AL");
        assertThat(p.esfera()).isEqualTo(Esfera.SENADO_FEDERAL);
    }
}
