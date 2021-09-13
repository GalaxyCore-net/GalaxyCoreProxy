CREATE TABLE IF NOT EXISTS `I18N_languages`
(
    `id`           INT AUTO_INCREMENT PRIMARY KEY,
    `lang`         VARCHAR(128),
    `head_data`    TEXT,
    `english_name` VARCHAR(128),
    `local_name`   VARCHAR(128),
    `date_fmt`     VARCHAR(128),
    `time_fmt`     VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS `I18N_language_data`
(
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `language_id` INT DEFAULT '0',
    `key`         VARCHAR(128),
    `value`       LONGTEXT
);

CREATE TABLE IF NOT EXISTS `I18N_player_data`
(
    `id`          INT PRIMARY KEY,
    `language_id` INT DEFAULT '0'
)