-- V0 base schema for HostelManagement (MySQL 8+).
-- Safe to run more than once. Creates the pre-RBAC hostel and room tables
-- needed before V1__rbac.sql on a fresh installation.

CREATE TABLE IF NOT EXISTS hostel (
    HOSTELID    INT          NOT NULL AUTO_INCREMENT,
    ADDRESS     VARCHAR(255) DEFAULT NULL,
    HOSTELNAME  VARCHAR(255) DEFAULT NULL,
    TOTALFLOORS INT          DEFAULT NULL,
    PRIMARY KEY (HOSTELID)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS room (
    ROOMID     BIGINT       NOT NULL AUTO_INCREMENT,
    CAPACITY   INT          DEFAULT NULL,
    MONTHLYFEE DOUBLE       DEFAULT NULL,
    ROOMNUMBER VARCHAR(255) DEFAULT NULL,
    STATUS     VARCHAR(255) DEFAULT NULL,
    hostel_id  INT          DEFAULT NULL,
    PRIMARY KEY (ROOMID),
    KEY FK_room_hostel_id (hostel_id),
    CONSTRAINT FK_room_hostel_id
        FOREIGN KEY (hostel_id) REFERENCES hostel (HOSTELID)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
