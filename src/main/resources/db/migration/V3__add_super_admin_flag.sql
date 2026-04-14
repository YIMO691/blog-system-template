ALTER TABLE users
ADD COLUMN super_admin BIT(1) NOT NULL DEFAULT b'0';

UPDATE users
SET super_admin = b'1'
WHERE username = 'admin';
