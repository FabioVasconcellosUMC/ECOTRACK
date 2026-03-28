CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT NOW()
);

CREATE TABLE empresa (
    id BIGSERIAL PRIMARY KEY,
    razao_social VARCHAR(200) NOT NULL,
    cnpj VARCHAR(18) UNIQUE NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    endereco VARCHAR(300),
    email VARCHAR(150),
    telefone VARCHAR(20),
    ativa BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT NOW()
);

CREATE TABLE lote (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    tipo_residuo VARCHAR(100) NOT NULL,
    quantidade DECIMAL(10,2) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AGUARDANDO_COLETA',
    empresa_geradora_id BIGINT NOT NULL REFERENCES empresa(id),
    criado_por BIGINT REFERENCES usuario(id),
    criado_em TIMESTAMP DEFAULT NOW()
);

CREATE TABLE historico_lote (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES lote(id),
    status_anterior VARCHAR(50),
    status_novo VARCHAR(50) NOT NULL,
    usuario_id BIGINT REFERENCES usuario(id),
    observacao TEXT,
    data_hora TIMESTAMP DEFAULT NOW()
);

CREATE TABLE transporte (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES lote(id),
    transportadora_id BIGINT NOT NULL REFERENCES empresa(id),
    receptora_id BIGINT NOT NULL REFERENCES empresa(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    data_coleta TIMESTAMP,
    data_entrega TIMESTAMP,
    responsavel VARCHAR(150),
    criado_em TIMESTAMP DEFAULT NOW()
);