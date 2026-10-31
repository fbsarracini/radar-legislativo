CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE candidato (
    sq_candidato    VARCHAR(20)  PRIMARY KEY,
    ano_eleicao     INTEGER      NOT NULL,
    nome            VARCHAR(255),
    nome_urna       VARCHAR(255),
    cargo           VARCHAR(100),
    uf              VARCHAR(2),
    municipio       VARCHAR(255),
    partido         VARCHAR(30),
    numero          VARCHAR(10),
    situacao        VARCHAR(100),
    eleito          BOOLEAN      NOT NULL,
    nome_busca      VARCHAR(520) NOT NULL,
    cargo_busca     VARCHAR(100) NOT NULL,
    municipio_busca VARCHAR(255) NOT NULL,
    -- Marca da importação que gravou a linha; a seguinte do mesmo ano apaga as que não vieram de novo.
    importado_em    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_candidato_ano_uf ON candidato (ano_eleicao, uf);
CREATE INDEX ix_candidato_nome_urna ON candidato (nome_urna, sq_candidato);
-- Busca por trecho do nome (LIKE '%silva%') em ~450 mil linhas de um ano municipal.
CREATE INDEX ix_candidato_nome_busca_trgm ON candidato USING gin (nome_busca gin_trgm_ops);
