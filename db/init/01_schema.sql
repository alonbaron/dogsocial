-- dog-social MySQL schema (MySQL 8.x)

SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(190) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('USER') NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  avatar_path VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS dogs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL,
  breed VARCHAR(80) NULL,
  bio VARCHAR(300) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_dogs_owner_id (owner_id),
  CONSTRAINT fk_dogs_owner FOREIGN KEY (owner_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS posts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  author_id BIGINT NOT NULL,
  dog_id BIGINT NULL,
  caption VARCHAR(300) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_posts_author_id_created_at (author_id, created_at),
  KEY idx_posts_dog_id_created_at (dog_id, created_at),
  CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_posts_dog FOREIGN KEY (dog_id) REFERENCES dogs(id)
    ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  content VARCHAR(300) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_comments_post_id_created_at (post_id, created_at),
  KEY idx_comments_author_id_created_at (author_id, created_at),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_reactions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  type ENUM('LIKE','DISLIKE') NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_post_reactions_post_user (post_id, user_id),
  KEY idx_post_reactions_post_id_type (post_id, type),
  KEY idx_post_reactions_user_id (user_id),
  CONSTRAINT fk_post_reactions_post FOREIGN KEY (post_id) REFERENCES posts(id)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT fk_post_reactions_user FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS comment_reactions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  type ENUM('LIKE','DISLIKE') NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_comment_reactions_comment_user (comment_id, user_id),
  KEY idx_comment_reactions_comment_id_type (comment_id, type),
  KEY idx_comment_reactions_user_id (user_id),
  CONSTRAINT fk_comment_reactions_comment FOREIGN KEY (comment_id) REFERENCES comments(id)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT fk_comment_reactions_user FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS follows (
  id BIGINT NOT NULL AUTO_INCREMENT,
  follower_id BIGINT NOT NULL,
  followed_id BIGINT NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_follows_follower_followed (follower_id, followed_id),
  KEY idx_follows_follower_id (follower_id),
  KEY idx_follows_followed_id (followed_id),
  CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT fk_follows_followed FOREIGN KEY (followed_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS playdates (
  id BIGINT NOT NULL AUTO_INCREMENT,
  requester_id BIGINT NOT NULL,
  recipient_id BIGINT NOT NULL,
  scheduled_at TIMESTAMP(3) NOT NULL,
  status ENUM('PENDING','APPROVED','DECLINED','CANCELED') NOT NULL,
  note VARCHAR(300) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_playdates_requester_id_status (requester_id, status),
  KEY idx_playdates_recipient_id_status (recipient_id, status),
  KEY idx_playdates_scheduled_at (scheduled_at),
  CONSTRAINT fk_playdates_requester FOREIGN KEY (requester_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_playdates_recipient FOREIGN KEY (recipient_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

