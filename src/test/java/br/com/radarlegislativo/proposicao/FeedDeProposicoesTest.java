package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.PostgresTestcontainer;
import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.Nomes;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Sem a transação do teste: o feed monta a resposta fora da transação, e é isso que precisa funcionar. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTestcontainer.class, GravadorDeProposicoes.class, ListarProposicoes.class, ConsultarProposicao.class})
@Testcontainers(disabledWithoutDocker = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FeedDeProposicoesTest {

    private static final Instant ONTEM = Instant.parse("2026-10-30T12:00:00Z");
    private static final Instant HOJE = Instant.parse("2026-10-31T12:00:00Z");
    private static final PageRequest PAGINA = PageRequest.of(0, 20);

    @Autowired
    private GravadorDeProposicoes gravador;
    @Autowired
    private ListarProposicoes listarProposicoes;
    @Autowired
    private ConsultarProposicao consultarProposicao;
    @Autowired
    private JdbcTemplate jdbc;

    private static Parlamentar deputado(String id, String nome, String partido, String uf) {
        return new Parlamentar("feed", id, Esfera.CAMARA_FEDERAL, "Câmara dos Deputados", nome, partido, uf, null);
    }

    private static Autor autor(String id, String nome) {
        return new Autor(nome, "Deputado(a)", id, true);
    }

    private void gravar(String id, String sigla, LocalDate data, Instant vistaEm, Autor... autores) {
        gravador.criar(new Proposicao("feed", id, Esfera.CAMARA_FEDERAL, "Câmara dos Deputados",
                sigla, Integer.valueOf(id), 2026, "Ementa " + id, data, null, null), List.of(autores), vistaEm);
    }

    /** Ids externos de quem passou no filtro, na ordem do feed; restringe à fonte do teste. */
    private List<String> feed(LocalDate desde, LocalDate ate, Instant novasDesde, List<String> tipos,
                              String uf, String partido, String autor, Long parlamentar) {
        Page<ProposicaoColetada> page = listarProposicoes.execute(
                new FiltroProposicoes(desde, ate, novasDesde, "feed", tipos, uf, partido, autor, parlamentar), PAGINA);
        return page.map(ProposicaoColetada::idExterno).getContent();
    }

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM proposicao WHERE fonte = 'feed'");
        jdbc.update("DELETE FROM parlamentar WHERE fonte = 'feed'");
        gravador.registrarParlamentar(deputado("1", "Maria do Rosário", "PT", "RS"), ONTEM);
        gravador.registrarParlamentar(deputado("2", "João Paulo", "PL", "SP"), ONTEM);
        gravador.registrarParlamentar(deputado("3", "Ana Paula", "PT", "SP"), ONTEM);
        gravar("101", "PL", LocalDate.of(2026, 10, 28), ONTEM, autor("1", "Maria do Rosário"));
        gravar("102", "PEC", LocalDate.of(2026, 10, 29), ONTEM, autor("1", "Maria do Rosário"), autor("2", "João Paulo"));
        gravar("103", "PL", LocalDate.of(2026, 10, 30), HOJE, autor("3", "Ana Paula"),
                new Autor("Poder Executivo", "Órgão do Poder Executivo", null, false));
    }

    @Test
    @DisplayName("deve deixar proposição sem data de apresentação no fim do feed")
    void deveDeixarSemDataNoFim() {
        gravar("100", "PDL", null, HOJE);

        assertThat(feed(null, null, null, List.of(), null, null, null, null)).containsExactly("103", "102", "101", "100");
        assertThat(listarProposicoes.execute(new FiltroProposicoes(null, null, null, "feed", null, null, null, null, null),
                PageRequest.of(1, 3)).getContent()).extracting(ProposicaoColetada::idExterno).containsExactly("100");
    }

    @Test
    @DisplayName("deve ordenar do mais recente e filtrar por período, tipo e novas desde")
    void deveFiltrarPorPeriodoTipoENovas() {
        assertThat(feed(null, null, null, List.of(), null, null, null, null)).containsExactly("103", "102", "101");
        assertThat(listarProposicoes.execute(new FiltroProposicoes(null, null, null, "feed", null, null, null, null, null),
                PageRequest.of(0, 2)).getTotalElements()).isEqualTo(3);
        assertThat(feed(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 10, 29), null, List.of(), null, null, null, null))
                .containsExactly("102");
        assertThat(feed(null, null, null, List.of("PL"), null, null, null, null)).containsExactly("103", "101");
        assertThat(feed(null, null, HOJE, List.of(), null, null, null, null)).containsExactly("103");
    }

    @Test
    @DisplayName("deve aplicar UF e partido ao mesmo autor")
    void deveAplicarUfEPartidoAoMesmoAutor() {
        // 102 tem um autor do PT (RS) e outro de SP (PL): não é "um autor do PT de SP".
        assertThat(feed(null, null, null, List.of(), "SP", "pt", null, null)).containsExactly("103");
        assertThat(feed(null, null, null, List.of(), "sp", null, null, null)).containsExactly("103", "102");
        assertThat(feed(null, null, null, List.of(), null, "PT", null, null)).containsExactly("103", "102", "101");
    }

    @Test
    @DisplayName("deve buscar autor sem diferenciar acento, inclusive quem não é parlamentar")
    void deveBuscarAutorSemAcento() {
        assertThat(feed(null, null, null, List.of(), null, null, "rosario", null)).containsExactly("102", "101");
        assertThat(feed(null, null, null, List.of(), null, null, "executivo", null)).containsExactly("103");
    }

    @Test
    @DisplayName("deve filtrar pelo id interno do parlamentar autor")
    void deveFiltrarPorParlamentar() {
        Long joao = jdbc.queryForObject("SELECT id FROM parlamentar WHERE fonte = 'feed' AND id_externo = '2'", Long.class);

        assertThat(feed(null, null, null, List.of(), null, null, null, joao)).containsExactly("102");
    }

    @Test
    @DisplayName("deve entregar autores e parlamentares carregados para uso fora da transação")
    void deveCarregarAutoresParaForaDaTransacao() {
        ProposicaoColetada pec = listarProposicoes.execute(
                new FiltroProposicoes(null, null, null, "feed", List.of("PEC"), null, null, null, null), PAGINA).getContent().getFirst();

        assertThat(pec.autores()).extracting(a -> a.parlamentar().partido()).containsExactly("PT", "PL");
        ProposicaoColetada detalhe = consultarProposicao.execute(pec.id());
        assertThat(detalhe.autores()).extracting(Autoria::nome).containsExactly("Maria do Rosário", "João Paulo");
        assertThat(detalhe.identificacao()).isEqualTo("PEC 102/2026");
    }

    @Test
    @DisplayName("o backfill do V3 deve normalizar nomes igual a Nomes.normalizar")
    void backfillDeveNormalizarIgualAoJava() {
        for (String nome : List.of("Maria do Rosário", "Dr. Francisco", "Prof.ª Conceição  D'Ávila", "Zé-Neto (PT)")) {
            String sql = jdbc.queryForObject("SELECT btrim(regexp_replace(regexp_replace(upper(unaccent(coalesce(?, ''))),"
                    + " '[^A-Z0-9 ]', ' ', 'g'), '\\s+', ' ', 'g'))", String.class, nome);
            assertThat(sql).as(nome).isEqualTo(Nomes.normalizar(nome));
        }
    }
}
