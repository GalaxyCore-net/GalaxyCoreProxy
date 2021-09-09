package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import java.util.UUID;

public interface I18NPort {

    void setDefault(String lang, String key, String value);
    String get(String lang, String key);
    void retrieve();
    I18N.MinecraftLocale getLocale(UUID uuid);

}
