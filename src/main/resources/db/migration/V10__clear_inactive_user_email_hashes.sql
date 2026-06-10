UPDATE usuario
SET email_hash = NULL
WHERE ativo = false
  AND email_hash IS NOT NULL;
