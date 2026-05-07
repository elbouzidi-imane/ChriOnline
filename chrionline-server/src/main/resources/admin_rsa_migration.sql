ALTER TABLE utilisateur
ADD COLUMN IF NOT EXISTS cle_publique_rsa TEXT;

-- Apres generation des cles avec AdminRsaKeyPairGenerator :
-- UPDATE utilisateur
-- SET cle_publique_rsa = '<CLE_PUBLIQUE_BASE64>'
-- WHERE role = 'ADMIN' AND email = 'admin@chrionline.com';
