package net.galaxycore.galaxycoreproxy.utils;

import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;

import java.sql.SQLException;

public class SQLUtils {

    public static void runScript(DatabaseConfiguration databaseConfiguration, String scope, String name) {

        try {
            for (String query : FileUtils.readSQLScript(scope, name,
                    databaseConfiguration.getInternalConfiguration().getConnection().equals("sqlite") ? "sqlite" : "mysql").split(";")) {

                query = query.replace("\n", "");
                databaseConfiguration.getConnection().prepareStatement(query).executeUpdate();

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
