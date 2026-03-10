-- Migration: convert ENUM columns to VARCHAR for Hibernate compatibility
-- Run this against an existing database if 01_schema.sql has already been applied.

ALTER TABLE users
  MODIFY COLUMN role VARCHAR(20) NOT NULL;

ALTER TABLE post_reactions
  MODIFY COLUMN type VARCHAR(20) NOT NULL;

ALTER TABLE comment_reactions
  MODIFY COLUMN type VARCHAR(20) NOT NULL;

ALTER TABLE playdates
  MODIFY COLUMN status VARCHAR(20) NOT NULL;
