package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;

public interface I18NPort {

    void setDefault(String lang, String key, String value);
    void setDefault(String lang, String key, String value, boolean usePrefix);
    String get(String lang, String key);
    String get(Player player, String key);
    void retrieve();
    HashMap<String, I18N.MinecraftLocale> getLanguages();

}
