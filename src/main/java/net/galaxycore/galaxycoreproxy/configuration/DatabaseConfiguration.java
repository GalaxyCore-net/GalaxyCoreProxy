package net.galaxycore.galaxycoreproxy.configuration;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfiguration {

    @Getter
    private final InternalConfiguration internalConfiguration;

    @Getter
    private Connection connection;

    public DatabaseConfiguration(InternalConfiguration internalConfiguration) {

        this.internalConfiguration = internalConfiguration;

        try {
            if(internalConfiguration.getConnection().equals("sqlite")) {

                connection = DriverManager.getConnection("jdbc:sqlite:" + internalConfiguration.getDataFolder().getAbsolutePath()
                        + "/" + internalConfiguration.getSqliteName(), "sa", "");

            }else {

                connection = DriverManager.getConnection("jdbc:mysql://" + internalConfiguration.getMysqlHost() + ":" + internalConfiguration.getMysqlPort() + "/"
                        + internalConfiguration.getMysqlDatabase() + "?autoReconnect=true", internalConfiguration.getMysqlUser(), internalConfiguration.getMysqlPassword());
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public ConfigNamespace getNamespace(String name) {
        return new ConfigNamespace("config_" + name, this);
    }

    public void disable() {
        try {
            if(connection != null && !connection.isClosed())
                connection.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
