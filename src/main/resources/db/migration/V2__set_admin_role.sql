UPDATE users
SET role = 'ROLE_ADMIN'
WHERE username = 'admin' AND (role IS NULL OR role = 'ROLE_USER');

UPDATE users
SET role = 'ROLE_USER'
WHERE role IS NULL;
