SELECT I18N_language_data.key, I18N_language_data.value, I18N_languages.lang
FROM I18N_language_data,
     I18N_languages
WHERE I18N_languages.id = I18N_language_data.language_id;