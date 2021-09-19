package net.galaxycore.galaxycoreproxy.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Getter
@Setter
public class InternalConfiguration {

    private String connection;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlUser;
    private String mysqlPassword;
    private String mysqlDatabase;
    private String sqliteName;

    private File dataFolder;

    public InternalConfiguration(File settingsDataFolder) {
        this.dataFolder = settingsDataFolder;

        if(!settingsDataFolder.exists())
            //noinspection ResultOfMethodCallIgnored
            settingsDataFolder.mkdirs();

        File configurationFile = new File(settingsDataFolder, "config.yml");

        if(!configurationFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                configurationFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            YAMLConfiguration yamlConfiguration = new YAMLConfiguration();
            try {
                yamlConfiguration.read(configurationFile.toURI().toURL().openStream());
                yamlConfiguration.setProperty("connection", "sqlite");
                yamlConfiguration.setProperty("mysql.host", "host");
                yamlConfiguration.setProperty("mysql.port", 3306);
                yamlConfiguration.setProperty("mysql.user", "user");
                yamlConfiguration.setProperty("mysql.password", "password");
                yamlConfiguration.setProperty("mysql.database", "database");
                yamlConfiguration.setProperty("sqlite.name", "TestDatabase.sqlite");
                yamlConfiguration.write(new FileWriter(configurationFile, false));
            } catch (ConfigurationException | IOException e) {
                e.printStackTrace();
            }

        }

        YAMLConfiguration config = new YAMLConfiguration();
        try {
            config.read(configurationFile.toURI().toURL().openStream());
            connection = config.getString("connection");
            mysqlHost = config.getString("mysql.host");
            mysqlPort = config.getInt("mysql.port");
            mysqlUser = config.getString("mysql.user");
            mysqlPassword = config.getString("mysql.password");
            mysqlDatabase = config.getString("mysql.database");
            sqliteName = config.getString("sqlite.name");
        } catch (ConfigurationException | IOException e) {
            e.printStackTrace();
        }

    }

}
