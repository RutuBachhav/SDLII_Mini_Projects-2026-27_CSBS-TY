-- V2 room preference, request, and allocation schema (MySQL 8+).
-- Pending requests do not consume capacity. Approved requests create a
-- room_allocation row and count against room capacity.

CREATE TABLE IF NOT EXISTS room_request (
    request_id   BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    room_id      BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at   DATETIME    NULL,
    decided_by   BIGINT      NULL,
    PRIMARY KEY (request_id),
    KEY idx_room_request_user_status (user_id, status),
    KEY idx_room_request_room_status (room_id, status),
    KEY idx_room_request_decided_by (decided_by),
    CONSTRAINT fk_room_request_user
        FOREIGN KEY (user_id) REFERENCES app_users (user_id),
    CONSTRAINT fk_room_request_room
        FOREIGN KEY (room_id) REFERENCES room (ROOMID) ON DELETE CASCADE,
    CONSTRAINT fk_room_request_decider
        FOREIGN KEY (decided_by) REFERENCES app_users (user_id)
        ON DELETE SET NULL,
    CONSTRAINT chk_room_request_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS room_allocation (
    allocation_id BIGINT   NOT NULL AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    room_id       BIGINT   NOT NULL,
    request_id    BIGINT   NOT NULL,
    allocated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    allocated_by  BIGINT   NULL,
    PRIMARY KEY (allocation_id),
    UNIQUE KEY uk_room_allocation_user (user_id),
    UNIQUE KEY uk_room_allocation_request (request_id),
    KEY idx_room_allocation_room (room_id),
    KEY idx_room_allocation_allocator (allocated_by),
    CONSTRAINT fk_room_allocation_user
        FOREIGN KEY (user_id) REFERENCES app_users (user_id),
    CONSTRAINT fk_room_allocation_room
        FOREIGN KEY (room_id) REFERENCES room (ROOMID) ON DELETE CASCADE,
    CONSTRAINT fk_room_allocation_request
        FOREIGN KEY (request_id) REFERENCES room_request (request_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_room_allocation_allocator
        FOREIGN KEY (allocated_by) REFERENCES app_users (user_id)
        ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
