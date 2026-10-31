-- Proposições que falharam na coleta (autores/parlamentar indisponíveis) e ficaram para a próxima.
ALTER TABLE coleta ADD COLUMN pendentes INTEGER NOT NULL DEFAULT 0;
