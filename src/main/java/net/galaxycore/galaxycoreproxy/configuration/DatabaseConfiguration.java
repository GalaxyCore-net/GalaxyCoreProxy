package net.galaxycore.galaxycoreproxy.configuration;

import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycorecore.utils.SqlUtils;
import net.galaxycore.galaxycoreproxy.utils.SQLUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfiguration {

    @Getter
    private final InternalConfiguration internalConfiguration;

    @Getter
    private Connection connection;

    @SneakyThrows
    public DatabaseConfiguration(InternalConfiguration internalConfiguration) {

        this.internalConfiguration = internalConfiguration;
        //IDK, but it doesn´t work without it
        Class.forName("com.mysql.cj.jdbc.Driver");

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

        SQLUtils.runScript(this, "core", "create");

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
