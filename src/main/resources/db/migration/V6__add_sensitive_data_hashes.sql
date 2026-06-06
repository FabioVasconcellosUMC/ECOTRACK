CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE usuario ALTER COLUMN nome TYPE TEXT;
ALTER TABLE usuario ALTER COLUMN email TYPE TEXT;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS email_hash VARCHAR(100);

UPDATE usuario
SET email_hash = encode(digest(lower(trim(email)), 'sha256'), 'hex')
WHERE email_hash IS NULL
  AND email IS NOT NULL
  AND email NOT LIKE 'enc:%';

ALTER TABLE empresa ALTER COLUMN cnpj TYPE TEXT;
ALTER TABLE empresa ALTER COLUMN endereco TYPE TEXT;
ALTER TABLE empresa ALTER COLUMN email TYPE TEXT;
ALTER TABLE empresa ALTER COLUMN telefone TYPE TEXT;
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS cnpj_hash VARCHAR(100);
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS email_hash VARCHAR(100);

UPDATE empresa
SET cnpj_hash = encode(digest(regexp_replace(cnpj, '\D', '', 'g'), 'sha256'), 'hex')
WHERE cnpj_hash IS NULL
  AND cnpj IS NOT NULL
  AND cnpj NOT LIKE 'enc:%';

UPDATE empresa
SET email_hash = encode(digest(lower(trim(email)), 'sha256'), 'hex')
WHERE email_hash IS NULL
  AND email IS NOT NULL
  AND email NOT LIKE 'enc:%';

ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_email_key;
ALTER TABLE empresa DROP CONSTRAINT IF EXISTS empresa_cnpj_key;

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_email_hash ON usuario(email_hash) WHERE email_hash IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_empresa_cnpj_hash ON empresa(cnpj_hash) WHERE cnpj_hash IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_empresa_email_hash ON empresa(email_hash);
