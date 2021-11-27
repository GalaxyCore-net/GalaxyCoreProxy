package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycorecore.utils.FileUtils;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.PrefixProvider;
import net.galaxycore.galaxycoreproxy.utils.SQLUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class I18N implements I18NPort {

    @Getter
    private static final I18NProvider instanceRef = new I18NProvider();

    private final DatabaseConfiguration databaseConfiguration;

    @Getter
    private final HashMap<String, I18N.MinecraftLocale> languages = new HashMap<>();
    private final HashMap<String, Boolean> usePrefix = new HashMap<>();
    private ConcurrentHashMap<I18N.MinecraftLocale, HashMap<String, String>> languageData = new ConcurrentHashMap<>();

    private I18N(GalaxyCoreProxy proxy) {
        databaseConfiguration = proxy.getDatabaseConfiguration();
        Logger logger = Logger.getLogger(this.getClass().getName());


        SQLUtils.runScript(databaseConfiguration, "i18n", "initialize");

        try {
            if (!databaseConfiguration.getConnection().prepareStatement("SELECT id FROM I18N_languages;").executeQuery().next()) {
                databaseConfiguration.getConnection().prepareStatement("INSERT INTO I18N_languages (lang, head_data, english_name, local_name, date_fmt, time_fmt) VALUES ('de_DE', '5e7899b4806858697e283f084d9173fe487886453774626b24bd8cfecc77b3f', 'German', 'Deutsch', 'DD.MM.YYYY', 'mm:ss');").executeUpdate();
                databaseConfiguration.getConnection().prepareStatement("INSERT INTO I18N_languages (lang, head_data, english_name, local_name, date_fmt, time_fmt) VALUES ('en_GB', 'a1701f21835a898b20759fb30a583a38b994abf60d3912ab4ce9f2311e74f72', 'English', 'English', 'DD.MM.YYYY', 'mm:ss');").executeUpdate();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {
            ResultSet resultSet =
                    databaseConfiguration.getConnection().
                            prepareStatement("SELECT * FROM `I18N_languages`;").executeQuery();

            while (resultSet.next()) {
                languages.put(resultSet.getString("lang"), new I18N.MinecraftLocale(
                        resultSet.getInt("id"),
                        resultSet.getString("lang"),
                        resultSet.getString("head_data"),
                        resultSet.getString("english_name"),
                        resultSet.getString("local_name"),
                        resultSet.getString("date_fmt"),
                        resultSet.getString("time_fmt")
                ));

                logger.info(String.format("Registered MinecraftLocale %s", resultSet.getString("lang")));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void init(GalaxyCoreProxy proxy) {
        if (instanceRef.get() == null) {
            I18NProvider.setI18N(new I18N(proxy));
        }
    }

    public static void setDefaultByLang(String lang, String key, String value) {
        instanceRef.get().setDefault(lang, key, value);
    }

    public static void setDefaultByLang(String lang, String key, String value, boolean usePrefix) {
        instanceRef.get().setDefault(lang, key, value, usePrefix);
    }

    public static String getByLang(String lang, String key) {
        return instanceRef.get().get(lang, key);
    }

    public static String getByPlayer(Player player, String key) {
        return instanceRef.get().get(player, key);
    }

    public String get(String lang, String key) {
        return (usePrefix.getOrDefault(key, false)? PrefixProvider.getPrefix() : "") + languageData.get(languages.get(lang)).get(key);
    }

    public String get(Player player, String key) {
        return get(getLocale(player), key);
    }

    private String getLocale(Player player) {
        return I18NPlayerLoader.getLocale(player);
    }


    public void setDefault(String lang, String key, String value) {
        MinecraftLocale locale = languages.get(lang);

        languageData.computeIfAbsent(locale, k -> new HashMap<>());

        HashMap<String, String> localizedBundle = languageData.get(locale);
        localizedBundle.put(key, value);
    }

    @Override
    public void setDefault(String lang, String key, String value, boolean usePrefix) {
        this.usePrefix.put(key, usePrefix);
        setDefault(lang, key, value);
    }

    public void retrieve() {
        languageData.forEach((minecraftLocale, localizedKV) -> localizedKV.forEach((key, value) -> {
            try {

                PreparedStatement isAvaiable = databaseConfiguration.getConnection().prepareStatement("SELECT id FROM I18N_language_data WHERE `language_id`=? AND `key`=?;");

                isAvaiable.setInt(1, minecraftLocale.id);
                isAvaiable.setString(2, key);

                if (!isAvaiable.executeQuery().next()) {
                    PreparedStatement stmt = databaseConfiguration.getConnection().prepareStatement(
                            "INSERT INTO `I18N_language_data` (`language_id`, `key`, `value`) VALUES (?, ?, ?)"
                    );

                    stmt.setInt(1, minecraftLocale.id);
                    stmt.setString(2, key);
                    stmt.setString(3, value);

                    stmt.executeUpdate();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }));

        try {
            ResultSet resultSet = databaseConfiguration.getConnection().prepareStatement(
                    FileUtils.readSqlScript(
                            "i18n",
                            "loadAll",
                            databaseConfiguration.getInternalConfiguration().getConnection()
                                    .equals("sqlite") ? "sqlite" : "mysql"
                    )
            ).executeQuery();

            languageData = new ConcurrentHashMap<>();

            while (resultSet.next()) {
                MinecraftLocale locale = languages.get(resultSet.getString("lang"));

                languageData.computeIfAbsent(locale, minecraftLocale -> new HashMap<>());

                languageData.get(locale).put(resultSet.getString("key"), resultSet.getString("value"));
            }

            resultSet.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public static void load(){
        instanceRef.get().retrieve();
    }

    @Getter
    public static class MinecraftLocale {
        private final int id;
        private final String lang;
        private final String headData;
        private final String englishName;
        private final String localName;
        private final String dateFormat;
        private final String timeFormat;

        public MinecraftLocale(int id, String lang, String headData, String englishName, String localName, String dateFormat, String timeFormat) {
            this.id = id;
            this.lang = lang;
            this.headData = headData;
            this.englishName = englishName;
            this.localName = localName;
            this.dateFormat = dateFormat;
            this.timeFormat = timeFormat;
        }
    }

}
