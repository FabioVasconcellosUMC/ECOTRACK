CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE empresa ADD COLUMN IF NOT EXISTS public_id UUID;
UPDATE empresa SET public_id = gen_random_uuid() WHERE public_id IS NULL;
ALTER TABLE empresa ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_empresa_public_id ON empresa(public_id);

ALTER TABLE lote ADD COLUMN IF NOT EXISTS public_id UUID;
UPDATE lote SET public_id = gen_random_uuid() WHERE public_id IS NULL;
ALTER TABLE lote ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_lote_public_id ON lote(public_id);

ALTER TABLE transporte ADD COLUMN IF NOT EXISTS public_id UUID;
UPDATE transporte SET public_id = gen_random_uuid() WHERE public_id IS NULL;
ALTER TABLE transporte ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_transporte_public_id ON transporte(public_id);
