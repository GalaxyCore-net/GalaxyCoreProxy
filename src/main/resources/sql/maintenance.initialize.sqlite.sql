CREATE TABLE IF NOT EXISTS `maintenance_players`
(
    id        INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    uuid      VARCHAR(255)                       NOT NULL,
    beta      BOOLEAN DEFAULT 0                  NOT NULL,
    emergency BOOLEAN DEFAULT 0                  NOT NULL
)