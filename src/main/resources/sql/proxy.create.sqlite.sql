CREATE TABLE IF NOT EXISTS `core_playercache`
(
    `id`                INTEGER PRIMARY KEY,
    `uuid`              VARCHAR(255) NOT NULL,
    `lastname`          VARCHAR(255) NOT NULL,
    `firstlogin`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lastlogin`         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_daily_reward` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `banpoints`         INTEGER      NOT NULL DEFAULT 0,
    `mutepoints`        INTEGER      NOT NULL DEFAULT 0,
    `warnpoints`        INTEGER      NOT NULL DEFAULT 0,
    `reports`           INTEGER      NOT NULL DEFAULT 0,
    `teamlogin`         BOOLEAN      NOT NULL DEFAULT false,
    `debug`             BOOLEAN      NOT NULL DEFAULT false,
    `socialspy`         BOOLEAN      NOT NULL DEFAULT false,
    `commandspy`        BOOLEAN      NOT NULL DEFAULT false,
    `vanished`          BOOLEAN      NOT NULL DEFAULT false,
    `nicked`            BOOLEAN      NOT NULL DEFAULT false,
    `lastnick`          INTEGER      NOT NULL DEFAULT 0,
    `coins`             INTEGER      NOT NULL DEFAULT 10000
);

CREATE TABLE IF NOT EXISTS `core_bans`
(
    `id`        INTEGER PRIMARY KEY,
    `userid`    INTEGER   NOT NULL,
    `reasonid`  INTEGER   NOT NULL,
    `banpoints` INTEGER   NOT NULL,
    `from`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `until`     TIMESTAMP,
    `permanent` BOOLEAN   NOT NULL DEFAULT false,
    `staff`     INTEGER
);

CREATE TABLE IF NOT EXISTS `core_mutes`
(
    `id`         INTEGER PRIMARY KEY,
    `userid`     INTEGER      NOT NULL,
    `reasonid`   INTEGER      NOT NULL,
    `mutepoints` INTEGER      NOT NULL,
    `from`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `until`      TIMESTAMP,
    `permanent`  BOOLEAN      NOT NULL DEFAULT false,
    `staff`      INTEGER,
    `message`    VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS `core_warns`
(
    `id`         INTEGER PRIMARY KEY,
    `userid`     INTEGER   NOT NULL,
    `reasonid`   INTEGER   NOT NULL,
    `warnpoints` INTEGER   NOT NULL,
    `date`       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `staff`      INTEGER
);

CREATE TABLE IF NOT EXISTS `core_reports`
(
    `id`       INTEGER PRIMARY KEY,
    `reported` INTEGER   NOT NULL,
    `reporter` INTEGER,
    `date`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `resolved` BOOLEAN   NOT NULL DEFAULT false,
    `staff`    INT
);

CREATE TABLE IF NOT EXISTS `core_punishment_reasons`
(
    `id`                       INTEGER PRIMARY KEY,
    `name`                     VARCHAR(255) NOT NULL,
    `required_permission_warn` VARCHAR(255) NOT NULL,
    `required_permission_mute` VARCHAR(255) NOT NULL,
    `required_permission_ban`  VARCHAR(255) NOT NULL,
    `points`                   INTEGER      NOT NULL,
    `points_increase_percent`  INTEGER      NOT NULL,
    `duration`                 INTEGER,
    `permanent`                BOOLEAN      NOT NULL
);

CREATE TABLE IF NOT EXISTS `core_coins_transactions`
(
    `id`                  INTEGER PRIMARY KEY,
    `player`              INTEGER      NOT NULL,
    `transaction_partner` INTEGER,
    `amount`              INTEGER      NOT NULL,
    `reason`              VARCHAR(255) NOT NULL
)