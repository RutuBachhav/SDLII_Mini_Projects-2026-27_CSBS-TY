-- V1 RBAC schema for HostelManagement (MySQL 8+).
-- Safe to run more than once. It does not modify existing hostel/room data.

CREATE TABLE IF NOT EXISTS app_users (
    user_id       BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(60)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_app_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS app_roles (
    role_id   BIGINT      NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(30) NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_app_roles_role_name (role_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES app_users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES app_roles (role_id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO app_roles (role_name) VALUES ('ADMIN'), ('WARDEN'), ('VIEWER');
