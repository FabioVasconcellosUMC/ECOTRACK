CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS ix_empresa_criado_em_desc
    ON empresa (criado_em DESC);

CREATE INDEX IF NOT EXISTS ix_empresa_tipo
    ON empresa (tipo);

CREATE INDEX IF NOT EXISTS ix_empresa_razao_social_trgm
    ON empresa USING gin (lower(razao_social) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_lote_criado_em_desc
    ON lote (criado_em DESC);

CREATE INDEX IF NOT EXISTS ix_lote_status
    ON lote (status);

CREATE INDEX IF NOT EXISTS ix_lote_empresa_geradora_criado_em_desc
    ON lote (empresa_geradora_id, criado_em DESC);

CREATE INDEX IF NOT EXISTS ix_lote_empresa_geradora_status
    ON lote (empresa_geradora_id, status);

CREATE INDEX IF NOT EXISTS ix_lote_descricao_trgm
    ON lote USING gin (lower(descricao) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_lote_tipo_residuo_trgm
    ON lote USING gin (lower(tipo_residuo) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_transporte_criado_em_desc
    ON transporte (criado_em DESC);

CREATE INDEX IF NOT EXISTS ix_transporte_status
    ON transporte (status);

CREATE INDEX IF NOT EXISTS ix_transporte_lote_id
    ON transporte (lote_id);

CREATE INDEX IF NOT EXISTS ix_transporte_lote_status
    ON transporte (lote_id, status);

CREATE INDEX IF NOT EXISTS ix_transporte_transportadora_criado_em_desc
    ON transporte (transportadora_id, criado_em DESC);

CREATE INDEX IF NOT EXISTS ix_transporte_transportadora_status
    ON transporte (transportadora_id, status);

CREATE INDEX IF NOT EXISTS ix_transporte_receptora_criado_em_desc
    ON transporte (receptora_id, criado_em DESC);

CREATE INDEX IF NOT EXISTS ix_transporte_receptora_status
    ON transporte (receptora_id, status);

CREATE INDEX IF NOT EXISTS ix_transporte_responsavel_trgm
    ON transporte USING gin (lower(responsavel) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS ix_historico_lote_data_hora_desc
    ON historico_lote (lote_id, data_hora DESC);
