package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.PostgresTestcontainer;
import br.com.radarlegislativo.config.AgendamentoConfig;
import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.FonteLegislativa;
import br.com.radarlegislativo.fonte.FonteRegistry;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import br.com.radarlegislativo.proposicao.AutoriaResponse;
import br.com.radarlegislativo.proposicao.CandidaturaResponse;
import br.com.radarlegislativo.proposicao.FeedDeProposicoes;
import br.com.radarlegislativo.proposicao.FiltroProposicoes;
import br.com.radarlegislativo.proposicao.ListarProposicoes;
import br.com.radarlegislativo.proposicao.ParlamentarColetadoRepository;
import br.com.radarlegislativo.proposicao.ProposicaoColetadaResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static br.com.radarlegislativo.TestFixtures.candidato;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Usa anos e fonte exclusivos (1998, 2002, "vinc") e limpa o que gravou: sem a transação do teste,
 * nada é desfeito sozinho, e o banco é compartilhado com as outras classes de teste.
 *
 * O fluxo inteiro no banco: candidatos importados + parlamentares coletados → sugestões → decisão manual
 * → recálculo → selo no feed → perfil. Sem a transação do teste, como na app.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTestcontainer.class, AgendamentoConfig.class, VincularParlamentares.class, DecidirVinculo.class,
        BuscarVinculosDoCandidato.class, ListarProposicoesDoCandidato.class, ConsultarPerfilDoCandidato.class,
        ListarProposicoes.class, GravadorDeTeste.class})
@Testcontainers(disabledWithoutDocker = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class VinculosNoBancoTest {

    private static final Instant AGORA = Instant.parse("2026-10-31T12:00:00Z");

    @MockitoBean
    private FonteRegistry registry;

    @Autowired private VincularParlamentares vincularParlamentares;
    @Autowired private DecidirVinculo decidirVinculo;
    @Autowired private BuscarVinculosDoCandidato buscarVinculos;
    @Autowired private ConsultarPerfilDoCandidato consultarPerfil;
    @Autowired private ListarProposicoes listarProposicoes;
    @Autowired private CandidatoRepository candidatoRepository;
    @Autowired private ParlamentarColetadoRepository parlamentarRepository;
    @Autowired private GravadorDeTeste gravador;
    @Autowired private JdbcTemplate jdbc;

    private Long rosario;
    private Long ana;

    private static Parlamentar deputado(String id, String nome, String uf) {
        return new Parlamentar("vinc", id, Esfera.CAMARA_FEDERAL, "Câmara dos Deputados", nome, "PT", uf, null);
    }

    @BeforeEach
    void setUp() {
        FonteLegislativa camara = Mockito.mock(FonteLegislativa.class);
        when(camara.esfera()).thenReturn(Esfera.CAMARA_FEDERAL);
        when(registry.encontrar("vinc")).thenReturn(Optional.of(camara));

        candidatoRepository.substituirAno(1998, List.of(
                candidato("8001", 1998, "DEPUTADO FEDERAL", "RS", "MARIA DO ROSÁRIO", "ELEITO POR QP")));
        candidatoRepository.substituirAno(2002, List.of(
                candidato("9001", 2002, "DEPUTADO FEDERAL", "RS", "MARIA DO ROSÁRIO", "ELEITO POR QP"),
                candidato("9002", 2002, "DEPUTADO FEDERAL", "SP", "MARIA DO ROSÁRIO", "SUPLENTE"),   // homônima em outra UF
                candidato("9003", 2002, "SENADOR", "RS", "MARIA DO ROSÁRIO", "NÃO ELEITO"),         // outro cargo
                candidato("9004", 2002, "DEPUTADO FEDERAL", "SP", "ANA PAULA", "SUPLENTE")));
        rosario = gravador.parlamentar(deputado("1", "Maria do Rosário", "RS"));
        ana = gravador.parlamentar(deputado("2", "Ana Paula", "SP"));
        gravador.proposicao(new Proposicao("vinc", "501", Esfera.CAMARA_FEDERAL, "Câmara dos Deputados", "PL", 501, 2002,
                "Ementa", LocalDate.of(2002, 10, 29), null, null), List.of(new Autor("Maria do Rosário", "Deputado(a)", "1", true)));
    }

    @AfterEach
    void tearDown() {
        jdbc.update("DELETE FROM proposicao WHERE fonte = 'vinc'");
        jdbc.update("DELETE FROM parlamentar WHERE fonte = 'vinc'");
        jdbc.update("DELETE FROM candidato WHERE ano_eleicao IN (1998, 2002)");
    }

    private List<String> sqsDoParlamentar(Long id) {
        return jdbc.queryForList("SELECT sq_candidato FROM vinculo WHERE parlamentar_id = ? ORDER BY sq_candidato", String.class, id);
    }

    @Test
    @DisplayName("deve sugerir só candidatos do mesmo cargo e UF, em todas as eleições importadas")
    void deveSugerirPorCargoEUf() {
        vincularParlamentares.execute(List.of(rosario, ana));

        assertThat(sqsDoParlamentar(rosario)).containsExactly("8001", "9001");
        assertThat(sqsDoParlamentar(ana)).containsExactly("9004");
    }

    @Test
    @DisplayName("o recálculo deve preservar decisões e ser idempotente")
    void recalculoDevePreservarDecisoes() {
        vincularParlamentares.execute(List.of(rosario));
        Vinculo de1998 = buscarVinculos.execute("8001").getFirst();
        decidirVinculo.execute("8001", de1998.id(), new DecidirVinculoRequest(false));

        int total = vincularParlamentares.executeTodos();
        vincularParlamentares.executeTodos();

        assertThat(sqsDoParlamentar(rosario)).containsExactly("8001", "9001");
        assertThat(buscarVinculos.execute("8001")).singleElement()
                .satisfies(v -> assertThat(v.status()).isEqualTo(Vinculo.Status.REJEITADO));
        assertThat(total).isGreaterThanOrEqualTo(2); // 9001 (rosário) + 9004 (ana); o rejeitado não conta
    }

    @Test
    @DisplayName("a reimportação do TSE deve apagar o vínculo de quem saiu do arquivo")
    void reimportacaoDeveApagarVinculoDeQuemSaiu() {
        vincularParlamentares.execute(List.of(rosario));

        candidatoRepository.substituirAno(1998, List.of());

        assertThat(sqsDoParlamentar(rosario)).containsExactly("9001");
    }

    @Test
    @DisplayName("o feed deve trazer as candidaturas do autor, sem as rejeitadas")
    void feedDeveTrazerCandidaturas() {
        vincularParlamentares.execute(List.of(rosario));
        Long de1998 = buscarVinculos.execute("8001").getFirst().id();
        decidirVinculo.execute("8001", de1998, new DecidirVinculoRequest(false));

        FeedDeProposicoes feed = listarProposicoes.execute(
                new FiltroProposicoes(null, null, null, "vinc", null, null, null, null, null), PageRequest.of(0, 10));
        ProposicaoColetadaResponse pl = ProposicaoColetadaResponse.of(feed.pagina().getContent().getFirst(), feed.candidaturas());

        AutoriaResponse autora = pl.autores().getFirst();
        assertThat(autora.candidaturas()).extracting(CandidaturaResponse::sqCandidato).containsExactly("9001");
        assertThat(autora.candidaturas().getFirst().eleito()).isTrue();
        assertThat(autora.candidaturas().getFirst().status()).isEqualTo(Vinculo.Status.SUGERIDO);
    }

    @Test
    @DisplayName("o perfil deve trazer vínculos e projetos lidos do banco")
    void perfilDeveVirDoBanco() {
        vincularParlamentares.execute(List.of(rosario, ana));

        PerfilDoCandidatoResponse perfil = PerfilDoCandidatoResponse.of(consultarPerfil.execute("9001"));
        assertThat(perfil.vinculos()).extracting(v -> v.parlamentar().idExterno()).containsExactly("1");
        assertThat(perfil.proposicoes()).extracting(ProposicaoColetadaResponse::identificacao).containsExactly("PL 501/2002");
        assertThat(perfil.totalProposicoes()).isEqualTo(1);

        PerfilDoCandidatoResponse semProjetos = PerfilDoCandidatoResponse.of(consultarPerfil.execute("9004"));
        assertThat(semProjetos.vinculos()).hasSize(1);
        assertThat(semProjetos.proposicoes()).isEmpty();
        assertThat(parlamentarRepository.findById(ana)).isPresent();
    }
}
