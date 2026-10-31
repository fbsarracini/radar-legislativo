package br.com.radarlegislativo.proposicao;

import br.com.radarlegislativo.PostgresTestcontainer;
import br.com.radarlegislativo.fonte.Autor;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static br.com.radarlegislativo.TestFixtures.autorDeputado;
import static br.com.radarlegislativo.TestFixtures.deputado;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/** Sem a transação do teste: cada método do gravador comita a sua, como na coleta de verdade. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTestcontainer.class, GravadorDeProposicoes.class})
@Testcontainers(disabledWithoutDocker = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class GravadorDeProposicoesTest {

    private static final Instant ONTEM = Instant.parse("2026-10-30T12:00:00Z");
    private static final Instant HOJE = Instant.parse("2026-10-31T12:00:00Z");

    @Autowired
    private GravadorDeProposicoes gravador;
    @Autowired
    private ProposicaoColetadaRepository proposicoes;
    @Autowired
    private TransactionTemplate tx;

    private static Proposicao pl(String id, String ementa) {
        return new Proposicao("camara", id, Esfera.CAMARA_FEDERAL, "Câmara dos Deputados",
                "PL", 5660, 2026, ementa, LocalDate.of(2026, 10, 29), null, "https://camara.test/" + id);
    }

    @Test
    @DisplayName("deve gravar a proposição com autores, ligando só quem é parlamentar conhecido")
    void deveGravarComAutores() {
        Parlamentar fulana = deputado("9001", "Fulana");
        gravador.registrarParlamentar(fulana, ONTEM);
        List<Autor> autores = List.of(autorDeputado("9001", "Fulana"),
                new Autor("Poder Executivo", "Órgão do Poder Executivo", null, false));

        gravador.criar(pl("7001", "Altera o CDC."), autores, ONTEM);

        tx.executeWithoutResult(s -> {
            ProposicaoColetada p = proposicoes.findByFonteAndIdExterno("camara", "7001").orElseThrow();
            assertThat(p.primeiraVezVistaEm()).isEqualTo(ONTEM);
            assertThat(p.autores()).extracting(Autoria::ordem, Autoria::nome)
                    .containsExactly(tuple(1, "Fulana"), tuple(2, "Poder Executivo"));
            assertThat(p.autores().get(0).parlamentar().partido()).isEqualTo("XYZ");
            assertThat(p.autores().get(1).parlamentar()).isNull();
        });
    }

    @Test
    @DisplayName("deve atualizar a proposição existente sem mudar quando foi vista pela primeira vez")
    void deveAtualizarExistente() {
        gravador.criar(pl("7002", "Ementa antiga."), List.of(), ONTEM);

        boolean existia = gravador.atualizarSeExistir(pl("7002", "Ementa corrigida."), HOJE);

        assertThat(existia).isTrue();
        ProposicaoColetada p = proposicoes.findByFonteAndIdExterno("camara", "7002").orElseThrow();
        assertThat(p.ementa()).isEqualTo("Ementa corrigida.");
        assertThat(p.primeiraVezVistaEm()).isEqualTo(ONTEM);
        assertThat(p.atualizadoEm()).isEqualTo(HOJE);
        assertThat(gravador.atualizarSeExistir(pl("7999", "Nova."), HOJE)).isFalse();
    }

    @Test
    @DisplayName("deve dizer quais parlamentares ainda não estão no banco")
    void deveListarDesconhecidos() {
        gravador.registrarParlamentar(deputado("9101", "Conhecido"), ONTEM);

        assertThat(gravador.parlamentaresDesconhecidos("camara", List.of("9101", "9102"))).isEqualTo(Set.of("9102"));
        assertThat(gravador.parlamentaresDesconhecidos("camara", List.of())).isEmpty();
    }
}
