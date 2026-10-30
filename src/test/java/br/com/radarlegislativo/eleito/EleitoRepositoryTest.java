package br.com.radarlegislativo.eleito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.radarlegislativo.TestFixtures.eleito;
import static org.assertj.core.api.Assertions.assertThat;

class EleitoRepositoryTest {

    private final EleitoRepository repository = new EleitoRepository();

    @Test
    @DisplayName("deve substituir só os eleitos do ano importado")
    void deveSubstituirSoOAnoImportado() {
        repository.salvar(eleito("1", 2022, "SENADOR", "RJ", "ANTIGO 2022"));
        repository.salvar(eleito("2", 2026, "SENADOR", "RJ", "SAIU EM 2026"));

        repository.substituirAno(2026, List.of(eleito("3", 2026, "SENADOR", "SP", "NOVO 2026")));

        assertThat(repository.buscar(new FiltroEleitos(null, null, null, null, null)))
                .extracting(CandidatoEleito::sqCandidato)
                .containsExactly("1", "3");
    }

    @Test
    @DisplayName("deve filtrar por ano, UF, cargo e nome ignorando acentos")
    void deveFiltrar() {
        repository.salvar(eleito("1", 2026, "DEPUTADO FEDERAL", "SP", "JOSÉ SILVA"));
        repository.salvar(eleito("2", 2026, "DEPUTADO FEDERAL", "RJ", "JOSÉ SOUZA"));
        repository.salvar(eleito("3", 2026, "SENADOR", "SP", "JOSÉ SANTOS"));

        List<CandidatoEleito> result = repository.buscar(new FiltroEleitos(2026, "sp", "deputado", "jose", null));

        assertThat(result).extracting(CandidatoEleito::sqCandidato).containsExactly("1");
    }
}
