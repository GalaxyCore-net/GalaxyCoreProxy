package net.galaxycore.galaxycoreproxy.utils;

import net.galaxycore.galaxycorecore.utils.FileUtils;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;

import java.sql.SQLException;

public class SQLUtils {

    public static void runScript(DatabaseConfiguration databaseConfiguration, String scope, String name) {
        try {
            for(String query : FileUtils.readSqlScript(scope, name, databaseConfiguration.getInternalConfiguration().getConnection().equals("sqlite") ? "sqlite" : "mysql").split(";")) {
                query = query.replace("\n", "");
                if(!query.equals(""))
                    databaseConfiguration.getConnection().prepareStatement(query).executeUpdate();
            }
        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
