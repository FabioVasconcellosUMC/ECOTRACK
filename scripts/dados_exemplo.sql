-- ============================================
-- EcoTrack - Script de Dados de Exemplo
-- Execute este script no Supabase SQL Editor
-- Senha de todos os usuários: admin123
-- ============================================

-- Usuários
INSERT INTO usuario (nome, email, senha, perfil, ativo) VALUES
('Administrador', 'admin@ecotrack.com', '$2a$10$89uLxWDhOTbYcAdFUE8b6etywFAEF32MjzwnMevfOKaUXWoXtRwxq', 'ADMIN', true),
('João Geradora', 'joao@geradora.com', '$2a$10$89uLxWDhOTbYcAdFUE8b6etywFAEF32MjzwnMevfOKaUXWoXtRwxq', 'GERADORA', true),
('Maria Transportadora', 'maria@transportadora.com', '$2a$10$89uLxWDhOTbYcAdFUE8b6etywFAEF32MjzwnMevfOKaUXWoXtRwxq', 'TRANSPORTADORA', true),
('Carlos Receptora', 'carlos@receptora.com', '$2a$10$89uLxWDhOTbYcAdFUE8b6etywFAEF32MjzwnMevfOKaUXWoXtRwxq', 'RECEPTORA', true);

-- Empresas
INSERT INTO empresa (razao_social, cnpj, tipo, endereco, email, telefone, ativa) VALUES
('Empresa Geradora Ltda', '12.345.678/0001-99', 'GERADORA', 'Rua das Flores, 100 - Mogi das Cruzes/SP', 'contato@geradora.com', '(11) 99999-1111', true),
('Transportadora Rápida S.A.', '98.765.432/0001-11', 'TRANSPORTADORA', 'Av. Principal, 200 - São Paulo/SP', 'contato@transportadora.com', '(11) 99999-2222', true),
('Receptora Ambiental ME', '11.222.333/0001-44', 'RECEPTORA', 'Rua do Meio Ambiente, 300 - Guarulhos/SP', 'contato@receptora.com', '(11) 99999-3333', true);

-- Lotes
INSERT INTO lote (descricao, tipo_residuo, quantidade, unidade, status, empresa_geradora_id) VALUES
('Resíduos químicos do processo industrial', 'Químico', 150.00, 'KG', 'AGUARDANDO_COLETA', 1),
('Resíduos eletrônicos descartados', 'Eletrônico', 50.00, 'KG', 'EM_TRANSITO', 1),
('Resíduos orgânicos da produção', 'Orgânico', 200.00, 'KG', 'DESCARTADO', 1);

-- Histórico dos lotes
INSERT INTO historico_lote (lote_id, status_anterior, status_novo, observacao, data_hora) VALUES
(1, null, 'AGUARDANDO_COLETA', 'Lote criado', NOW()),
(2, null, 'AGUARDANDO_COLETA', 'Lote criado', NOW() - INTERVAL '2 days'),
(2, 'AGUARDANDO_COLETA', 'EM_TRANSITO', 'Coletado pela transportadora', NOW() - INTERVAL '1 day'),
(3, null, 'AGUARDANDO_COLETA', 'Lote criado', NOW() - INTERVAL '5 days'),
(3, 'AGUARDANDO_COLETA', 'EM_TRANSITO', 'Coletado pela transportadora', NOW() - INTERVAL '4 days'),
(3, 'EM_TRANSITO', 'DESCARTADO', 'Descartado na receptora', NOW() - INTERVAL '3 days');