CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  nickname VARCHAR(50) NULL,
  display_name VARCHAR(50) NULL,
  email VARCHAR(50) NULL,
  phone VARCHAR(20) NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  enabled BIT(1) NOT NULL DEFAULT b'1',
  muted BIT(1) NOT NULL DEFAULT b'0',
  email_verified BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_phone (phone),
  KEY idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(255) NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name),
  KEY idx_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tags_name (name),
  KEY idx_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE articles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  slug VARCHAR(220) NOT NULL,
  content TEXT NOT NULL,
  published BIT(1) NOT NULL DEFAULT b'0',
  author_id BIGINT NOT NULL,
  category_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  views BIGINT NOT NULL DEFAULT 0,
  likes INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_articles_slug (slug),
  KEY idx_articles_published (published),
  KEY idx_articles_created_at (created_at),
  KEY idx_articles_author (author_id),
  KEY idx_articles_category (category_id),
  CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES users (id),
  CONSTRAINT fk_articles_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE article_tags (
  article_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (article_id, tag_id),
  KEY idx_article_tags_tag (tag_id),
  CONSTRAINT fk_article_tags_article FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  user_id BIGINT NULL,
  parent_id BIGINT NULL,
  display_name VARCHAR(80) NOT NULL,
  content VARCHAR(500) NOT NULL,
  approved BIT(1) NOT NULL DEFAULT b'0',
  created_at DATETIME(6) NOT NULL,
  likes INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_comments_article (article_id),
  KEY idx_comments_user (user_id),
  KEY idx_comments_parent (parent_id),
  CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE article_likes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  article_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_article_likes_article_user (article_id, user_id),
  KEY idx_article_likes_user (user_id),
  CONSTRAINT fk_article_likes_article FOREIGN KEY (article_id) REFERENCES articles (id),
  CONSTRAINT fk_article_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comment_likes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_comment_likes_comment_user (comment_id, user_id),
  KEY idx_comment_likes_user (user_id),
  CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments (id),
  CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type VARCHAR(40) NOT NULL,
  recipient_id BIGINT NULL,
  message VARCHAR(300) NOT NULL,
  link VARCHAR(200) NULL,
  is_read BIT(1) NOT NULL DEFAULT b'0',
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_notifications_recipient (recipient_id),
  KEY idx_notifications_created_at (created_at),
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE login_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  time DATETIME(6) NOT NULL,
  ip VARCHAR(64) NULL,
  ua VARCHAR(512) NULL,
  success BIT(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (id),
  KEY idx_login_records_user (user_id),
  KEY idx_login_records_time (time),
  CONSTRAINT fk_login_records_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE action_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  time DATETIME(6) NOT NULL,
  action VARCHAR(128) NOT NULL,
  detail VARCHAR(512) NULL,
  PRIMARY KEY (id),
  KEY idx_action_logs_user (user_id),
  KEY idx_action_logs_time (time),
  CONSTRAINT fk_action_logs_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
