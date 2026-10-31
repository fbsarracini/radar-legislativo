package br.com.radarlegislativo;

import br.com.radarlegislativo.config.RadarProperties;
import br.com.radarlegislativo.candidato.Candidato;
import br.com.radarlegislativo.fonte.Esfera;
import br.com.radarlegislativo.fonte.Parlamentar;
import br.com.radarlegislativo.fonte.Proposicao;

import java.time.Duration;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {}

    public static RadarProperties props(String diretorioTse) {
        return new RadarProperties(
                new RadarProperties.Http(Duration.ofSeconds(5), Duration.ofSeconds(5), "teste"),
                new RadarProperties.Cors(List.of("http://localhost:5173")),
                new RadarProperties.Camara("https://camara.test/api/v2", List.of(57), 3),
                new RadarProperties.Senado("https://senado.test/dadosabertos", "codigoParlamentarAutor", List.of()),
                List.of(),
                new RadarProperties.Tse("https://tse.test/consulta_cand_{ano}.zip", diretorioTse));
    }

    public static Candidato deputadoFederal(String sq, String nome, String urna) {
        return new Candidato(sq, 2026, nome, urna, "DEPUTADO FEDERAL", "SP", "SÃO PAULO", "XYZ", "4010", "ELEITO");
    }

    public static Candidato eleito(String sq, int ano, String cargo, String uf, String urna) {
        return candidato(sq, ano, cargo, uf, urna, "ELEITO");
    }

    public static Candidato candidato(String sq, int ano, String cargo, String uf, String urna, String situacao) {
        return new Candidato(sq, ano, urna, urna, cargo, uf, null, "XYZ", "10", situacao);
    }

    public static Parlamentar deputado(String id, String nome) {
        return new Parlamentar("camara", id, Esfera.CAMARA_FEDERAL, "Câmara dos Deputados", nome, "XYZ", "SP", null);
    }

    public static Proposicao proposicao(String id, String sigla, Integer numero, Integer ano) {
        return new Proposicao("camara", id, Esfera.CAMARA_FEDERAL, "Câmara dos Deputados",
                sigla, numero, ano, "Ementa", null, null, null);
    }
}
