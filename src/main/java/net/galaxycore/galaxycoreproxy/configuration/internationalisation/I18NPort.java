package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;

public interface I18NPort {

    void setDefault(String lang, String key, String value);
    String get(String lang, String key);
    String get(Player player, String key);
    void retrieve();
    String getLocale(Player player);
    HashMap<String, I18N.MinecraftLocale> getLanguages();

}
