-- Nome do autor normalizado (Nomes.normalizar no Java) para o filtro "autor" do feed, sem acento.
CREATE EXTENSION IF NOT EXISTS unaccent;

ALTER TABLE proposicao_autor ADD COLUMN nome_busca VARCHAR(255) NOT NULL DEFAULT '';

-- Mesmo resultado de Nomes.normalizar para as linhas já coletadas; as novas vêm preenchidas pela app.
UPDATE proposicao_autor
SET nome_busca = btrim(regexp_replace(regexp_replace(upper(unaccent(coalesce(nome, ''))), '[^A-Z0-9 ]', ' ', 'g'), '\s+', ' ', 'g'));

ALTER TABLE proposicao_autor ALTER COLUMN nome_busca DROP DEFAULT;

CREATE INDEX ix_parlamentar_partido_uf ON parlamentar (partido, uf);
