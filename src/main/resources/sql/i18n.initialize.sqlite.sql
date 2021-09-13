CREATE TABLE IF NOT EXISTS `I18N_languages`
(
    `id`           INTEGER PRIMARY KEY,
    `lang`         VARCHAR NOT NULL,
    `head_data`    TEXT    NOT NULL,
    `english_name` VARCHAR NOT NULL,
    `local_name`   VARCHAR NOT NULL,
    `date_fmt`     VARCHAR NOT NULL,
    `time_fmt`     VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS `I18N_language_data`
(
    `id`          INTEGER PRIMARY KEY,
    `language_id` INT     NOT NULL DEFAULT '0',
    `key`         VARCHAR NOT NULL,
    `value`       LONGTEXT
);

CREATE TABLE IF NOT EXISTS `I18N_player_data`
(
    `id`          INT,
    `language_id` INT NOT NULL DEFAULT '0'
);