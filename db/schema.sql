-- FoundIt - campus lost and found
-- Schema for MySQL 8.
-- Run as:  mysql -u root -p < schema.sql

DROP DATABASE IF EXISTS foundit_db;
CREATE DATABASE foundit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE foundit_db;

-- ---------------------------------------------------------------- users

CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(80)  NOT NULL,
  email         VARCHAR(160) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
  active        TINYINT(1)   NOT NULL DEFAULT 1,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_users_email (email),
  KEY ix_users_role (role)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- categories

CREATE TABLE categories (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(60) NOT NULL,
  active     TINYINT(1)  NOT NULL DEFAULT 1,
  created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_categories_name (name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- items
-- One table holds both sides of the board. `type` says which side a row is on.
-- `private_marks` is never rendered on a public page: it is the detail a real
-- owner should be able to state, so it is only visible to the reporter and to
-- administrators.

CREATE TABLE items (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  reporter_id   INT          NOT NULL,
  type          ENUM('LOST','FOUND') NOT NULL,
  title         VARCHAR(120) NOT NULL,
  category_id   INT          NOT NULL,
  description   TEXT         NOT NULL,
  color         VARCHAR(40)       DEFAULT NULL,
  private_marks VARCHAR(255)      DEFAULT NULL,
  location      VARCHAR(120) NOT NULL,
  item_date     DATE         NOT NULL,
  status        ENUM('OPEN','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  resolved_at   DATETIME          DEFAULT NULL,
  CONSTRAINT fk_items_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
  CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories (id),
  KEY ix_items_type_status (type, status),
  KEY ix_items_category (category_id),
  KEY ix_items_created (created_at),
  FULLTEXT KEY ft_items_text (title, description)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- photos
-- Only the storage key lives in MySQL. The bytes live on disk under the
-- configured uploads directory and are served by PhotoServlet.

CREATE TABLE item_photos (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  item_id      INT          NOT NULL,
  storage_key  VARCHAR(160) NOT NULL,
  content_type VARCHAR(60)  NOT NULL,
  size_bytes   INT          NOT NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_photos_item FOREIGN KEY (item_id) REFERENCES items (id) ON DELETE CASCADE,
  UNIQUE KEY uq_photos_key (storage_key),
  KEY ix_photos_item (item_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- claims
-- `evidence` is private: it is readable by the claimant, by the reporter of the
-- claimed item and by administrators. Nothing else.

CREATE TABLE claims (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  item_id      INT          NOT NULL,
  claimant_id  INT          NOT NULL,
  evidence     TEXT         NOT NULL,
  contact      VARCHAR(120) NOT NULL,
  status       ENUM('PENDING','APPROVED','REJECTED','COMPLETED') NOT NULL DEFAULT 'PENDING',
  review_note  VARCHAR(255)      DEFAULT NULL,
  reviewed_by  INT               DEFAULT NULL,
  reviewed_at  DATETIME          DEFAULT NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_claims_item     FOREIGN KEY (item_id)     REFERENCES items (id),
  CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_id) REFERENCES users (id),
  CONSTRAINT fk_claims_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id),
  KEY ix_claims_item (item_id, status),
  KEY ix_claims_claimant (claimant_id, status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- matches
-- Suggestions produced by MatchService. A pair is stored once; the unique key
-- is what stops duplicates when matching is recomputed.

CREATE TABLE matches (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  lost_item_id  INT          NOT NULL,
  found_item_id INT          NOT NULL,
  score         INT          NOT NULL,
  reasons       VARCHAR(500) NOT NULL,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_matches_lost  FOREIGN KEY (lost_item_id)  REFERENCES items (id) ON DELETE CASCADE,
  CONSTRAINT fk_matches_found FOREIGN KEY (found_item_id) REFERENCES items (id) ON DELETE CASCADE,
  UNIQUE KEY uq_matches_pair (lost_item_id, found_item_id),
  KEY ix_matches_score (score)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- notifications
-- dedupe_key makes "notify once" a database guarantee instead of a hope, so a
-- page reload or a re-run of matching cannot fill somebody's inbox.

CREATE TABLE notifications (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id    INT          NOT NULL,
  kind       VARCHAR(40)  NOT NULL,
  message    VARCHAR(255) NOT NULL,
  link       VARCHAR(160)      DEFAULT NULL,
  is_read    TINYINT(1)   NOT NULL DEFAULT 0,
  dedupe_key VARCHAR(120) NOT NULL,
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  UNIQUE KEY uq_notif_dedupe (dedupe_key),
  KEY ix_notif_user (user_id, is_read, created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- returns

CREATE TABLE item_returns (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  item_id     INT          NOT NULL,
  claim_id    INT          NOT NULL,
  handled_by  INT          NOT NULL,
  note        VARCHAR(255)      DEFAULT NULL,
  returned_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ret_item  FOREIGN KEY (item_id)    REFERENCES items (id),
  CONSTRAINT fk_ret_claim FOREIGN KEY (claim_id)   REFERENCES claims (id),
  CONSTRAINT fk_ret_user  FOREIGN KEY (handled_by) REFERENCES users (id),
  UNIQUE KEY uq_ret_claim (claim_id),
  KEY ix_ret_item (item_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- audit

CREATE TABLE audit_log (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  actor_id    INT               DEFAULT NULL,
  action      VARCHAR(60)  NOT NULL,
  entity      VARCHAR(40)  NOT NULL,
  entity_id   INT               DEFAULT NULL,
  detail      VARCHAR(255)      DEFAULT NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL,
  KEY ix_audit_created (created_at),
  KEY ix_audit_entity (entity, entity_id)
) ENGINE=InnoDB;
