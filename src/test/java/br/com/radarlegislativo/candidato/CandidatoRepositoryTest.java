package br.com.radarlegislativo.candidato;

import br.com.radarlegislativo.PostgresTestcontainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.candidato;
import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresTestcontainer.class)
@Testcontainers(disabledWithoutDocker = true)
class CandidatoRepositoryTest {

    private static final PageRequest PRIMEIRA_PAGINA = PageRequest.of(0, 50, ListarCandidatos.POR_NOME_URNA);

    @Autowired
    private CandidatoRepository repository;

    private List<String> buscar(FiltroCandidatos filtro) {
        return repository.findAll(filtro.especificacao(), PRIMEIRA_PAGINA).map(Candidato::sqCandidato).getContent();
    }

    @Test
    @DisplayName("deve substituir só os candidatos do ano importado")
    void deveSubstituirSoOAnoImportado() {
        repository.substituirAno(2022, List.of(eleito("1", 2022, "SENADOR", "RJ", "ANTIGO 2022")));
        repository.substituirAno(2026, List.of(eleito("2", 2026, "SENADOR", "RJ", "SAIU EM 2026")));

        repository.substituirAno(2026, List.of(eleito("3", 2026, "SENADOR", "SP", "NOVO 2026")));

        assertThat(repository.findAll()).extracting(Candidato::sqCandidato).containsExactlyInAnyOrder("1", "3");
    }

    @Test
    @DisplayName("deve atualizar o candidato que vem de novo na importação")
    void deveAtualizarCandidatoReimportado() {
        repository.substituirAno(2026, List.of(candidato("1", 2026, "SENADOR", "SP", "FULANO", "2º TURNO")));

        repository.substituirAno(2026, List.of(candidato("1", 2026, "SENADOR", "SP", "FULANO", "ELEITO")));

        assertThat(repository.findById("1")).get()
                .satisfies(c -> {
                    assertThat(c.situacao()).isEqualTo("ELEITO");
                    assertThat(c.eleito()).isTrue();
                });
    }

    @Test
    @DisplayName("deve filtrar por ano, UF, cargo, nome sem acento e situação")
    void deveFiltrar() {
        repository.substituirAno(2026, List.of(
                eleito("1", 2026, "DEPUTADO FEDERAL", "SP", "JOSÉ SILVA"),
                eleito("2", 2026, "DEPUTADO FEDERAL", "RJ", "JOSÉ SOUZA"),
                eleito("3", 2026, "SENADOR", "SP", "JOSÉ SANTOS"),
                candidato("4", 2026, "DEPUTADO FEDERAL", "SP", "JOSÉ PEREIRA", "SUPLENTE")));

        assertThat(buscar(new FiltroCandidatos(2026, "sp", "deputado", "jose", null, null))).containsExactly("4", "1");
        assertThat(buscar(new FiltroCandidatos(2026, "sp", "deputado", "jose", null, true))).containsExactly("1");
        assertThat(buscar(new FiltroCandidatos(null, null, null, "souza", null, null))).containsExactly("2");
        assertThat(buscar(new FiltroCandidatos(null, null, null, "%", null, null))).hasSize(4);
    }

    @Test
    @DisplayName("deve paginar ordenando por nome de urna")
    void devePaginar() {
        repository.substituirAno(2026, List.of(
                eleito("1", 2026, "SENADOR", "SP", "CARLA"),
                eleito("2", 2026, "SENADOR", "SP", "ANA"),
                eleito("3", 2026, "SENADOR", "SP", "BRUNO")));

        Page<Candidato> pagina = repository.findAll(new FiltroCandidatos(null, null, null, null, null, null).especificacao(),
                PageRequest.of(1, 2, ListarCandidatos.POR_NOME_URNA));

        assertThat(pagina.getContent()).extracting(Candidato::nomeUrna).containsExactly("CARLA");
        assertThat(pagina.getTotalElements()).isEqualTo(3);
    }
}
