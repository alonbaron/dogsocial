-- Migration: add username + bio to users, image_path to posts

ALTER TABLE users
  ADD COLUMN username VARCHAR(30) NULL AFTER email,
  ADD COLUMN bio VARCHAR(300) NULL AFTER avatar_path,
  ADD UNIQUE KEY uk_users_username (username);

ALTER TABLE posts
  ADD COLUMN image_path VARCHAR(500) NULL AFTER caption;
