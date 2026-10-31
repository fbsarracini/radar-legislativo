package br.com.radarlegislativo.candidato;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Upsert via JDBC em vez de {@code saveAll}: com id atribuído (sq do TSE), o JPA faria um SELECT por linha.
 * Fragmento do {@link CandidatoRepository} (o sufixo Impl é a convenção do Spring Data).
 */
class SubstituicaoDeCandidatosImpl implements SubstituicaoDeCandidatos {

    private static final int LOTE = 1_000;

    private static final String UPSERT = """
            INSERT INTO candidato (sq_candidato, ano_eleicao, nome, nome_urna, cargo, uf, municipio, partido, numero,
                                   situacao, eleito, nome_busca, cargo_busca, municipio_busca, importado_em)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (sq_candidato) DO UPDATE SET
                ano_eleicao = EXCLUDED.ano_eleicao, nome = EXCLUDED.nome, nome_urna = EXCLUDED.nome_urna,
                cargo = EXCLUDED.cargo, uf = EXCLUDED.uf, municipio = EXCLUDED.municipio,
                partido = EXCLUDED.partido, numero = EXCLUDED.numero, situacao = EXCLUDED.situacao,
                eleito = EXCLUDED.eleito, nome_busca = EXCLUDED.nome_busca, cargo_busca = EXCLUDED.cargo_busca,
                municipio_busca = EXCLUDED.municipio_busca, importado_em = EXCLUDED.importado_em
            """;

    private final JdbcTemplate jdbc;

    SubstituicaoDeCandidatosImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void substituirAno(int ano, List<Candidato> novos) {
        // O Postgres guarda microssegundos; truncar garante que a comparação abaixo veja o mesmo valor gravado.
        Timestamp agora = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.MICROS));
        jdbc.batchUpdate(UPSERT, novos, LOTE, (ps, c) -> {
            ps.setString(1, c.sqCandidato());
            ps.setInt(2, c.anoEleicao());
            ps.setString(3, c.nome());
            ps.setString(4, c.nomeUrna());
            ps.setString(5, c.cargo());
            ps.setString(6, c.uf());
            ps.setString(7, c.municipio());
            ps.setString(8, c.partido());
            ps.setString(9, c.numero());
            ps.setString(10, c.situacao());
            ps.setBoolean(11, c.eleito());
            ps.setString(12, c.nomeBusca());
            ps.setString(13, c.cargoBusca());
            ps.setString(14, c.municipioBusca());
            ps.setTimestamp(15, agora);
        });
        jdbc.update("DELETE FROM candidato WHERE ano_eleicao = ? AND importado_em < ?", ano, agora);
    }
}
