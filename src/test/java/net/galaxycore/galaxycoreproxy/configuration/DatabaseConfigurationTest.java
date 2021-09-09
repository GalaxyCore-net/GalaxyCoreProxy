package net.galaxycore.galaxycoreproxy.configuration;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseConfigurationTest {

    @Test
    void testDatabaseConfiguration() {
        File dataFolder = new File("run/tests/datafolderdatabase");

        if (dataFolder.exists()) {
            try {
                FileUtils.deleteDirectory(dataFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //noinspection ResultOfMethodCallIgnored
        dataFolder.mkdirs();

        InternalConfiguration internalConfigurationMock = mock(InternalConfiguration.class);
        when(internalConfigurationMock.getConnection()).thenReturn("sqlite");
        when(internalConfigurationMock.getSqliteName()).thenReturn("TestDatabase.sqlite");
        when(internalConfigurationMock.getMysqlHost()).thenReturn("");
        when(internalConfigurationMock.getMysqlPort()).thenReturn(3306);
        when(internalConfigurationMock.getMysqlUser()).thenReturn("");
        when(internalConfigurationMock.getMysqlPassword()).thenReturn("");
        when(internalConfigurationMock.getMysqlDatabase()).thenReturn("");
        when(internalConfigurationMock.getDataFolder()).thenReturn(dataFolder);

        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(internalConfigurationMock);

        ConfigNamespace namespace = databaseConfiguration.getNamespace("Test");

        namespace.set("testkey", "testvalue");

        assertEquals(namespace.get("testkey"), "testvalue");

        if (internalConfigurationMock.getConnection().equals("sqlite"))
            assertTrue(new File(dataFolder, "TestDatabase.sqlite").exists());

        databaseConfiguration.disable();
    }

}