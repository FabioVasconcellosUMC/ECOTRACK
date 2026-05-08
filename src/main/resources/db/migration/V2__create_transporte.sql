CREATE TABLE transporte (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES lote(id),
    transportadora_id BIGINT NOT NULL REFERENCES empresa(id),
    receptora_id BIGINT NOT NULL REFERENCES empresa(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    data_coleta TIMESTAMP,
    data_entrega TIMESTAMP,
    responsavel VARCHAR(150),
    observacao TEXT,
    criado_em TIMESTAMP DEFAULT NOW()
);