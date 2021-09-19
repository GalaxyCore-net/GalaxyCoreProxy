package net.galaxycore.galaxycoreproxy.configuration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigNamespace {

    private final String name;
    private final DatabaseConfiguration configuration;

    public ConfigNamespace(String name, DatabaseConfiguration configuration) {
        this.name = name;
        this.configuration = configuration;

        try {
            PreparedStatement statement = configuration.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + name + "(`key` VARCHAR(128), `value` VARCHAR(1024));");
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void setDefault(String key, String value) {
        if(get(key) == null)
            set(key, value);
    }

    public String get(String key) {
        try {

            PreparedStatement statement = configuration.getConnection().prepareStatement("SELECT `value` FROM " + name + " WHERE `key` = ?;");
            statement.setString(1, key);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next())
                return resultSet.getString("value");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void set(String key, String value) {
        try {
            PreparedStatement statement = configuration.getConnection().prepareStatement("SELECT `value` FROM " + name + " WHERE `key` = ?;");
            statement.setString(1, key);

            if(statement.executeQuery().next())
                statement = configuration.getConnection().prepareStatement("UPDATE " + name + " SET `value` = ? WHERE `key` = ?;");
            else
                statement = configuration.getConnection().prepareStatement("INSERT INTO " + name + " (`value`, `key`) VALUES (?, ?);");

            statement.setString(1, value);
            statement.setString(2, key);

            statement.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
