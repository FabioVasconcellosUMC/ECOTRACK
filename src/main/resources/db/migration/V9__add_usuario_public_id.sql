CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS public_id UUID;
UPDATE usuario SET public_id = gen_random_uuid() WHERE public_id IS NULL;
ALTER TABLE usuario ALTER COLUMN public_id SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_public_id ON usuario(public_id);
