CREATE TABLE user_admin_permissions (
  user_id BIGINT NOT NULL,
  permission VARCHAR(50) NOT NULL,
  PRIMARY KEY (user_id, permission),
  CONSTRAINT fk_user_admin_permissions_user
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO user_admin_permissions (user_id, permission)
SELECT id, 'ARTICLE_WRITE'
FROM users
WHERE role = 'ROLE_ADMIN';

INSERT INTO user_admin_permissions (user_id, permission)
SELECT id, 'ARTICLE_MANAGE'
FROM users
WHERE role = 'ROLE_ADMIN';

INSERT INTO user_admin_permissions (user_id, permission)
SELECT id, 'COMMENT_MODERATE'
FROM users
WHERE role = 'ROLE_ADMIN';

INSERT INTO user_admin_permissions (user_id, permission)
SELECT id, 'NOTIFICATION_MANAGE'
FROM users
WHERE role = 'ROLE_ADMIN';

INSERT INTO user_admin_permissions (user_id, permission)
SELECT id, 'STATS_VIEW'
FROM users
WHERE role = 'ROLE_ADMIN';

INSERT INTO user_admin_permissions (user_id, permission)
SELECT id, 'USER_MANAGE'
FROM users
WHERE super_admin = b'1';
