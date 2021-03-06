package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.InternalConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class I18NTest {

    @Test
    void init() {
        File dataFolder = new File("run/tests/i18ntest");

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
        when(internalConfigurationMock.getMysqlPort()).thenReturn(0);
        when(internalConfigurationMock.getMysqlUser()).thenReturn("");
        when(internalConfigurationMock.getMysqlPassword()).thenReturn("");
        when(internalConfigurationMock.getMysqlDatabase()).thenReturn("");
        when(internalConfigurationMock.getDataFolder()).thenReturn(dataFolder);

        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(internalConfigurationMock);

        GalaxyCoreProxy mainMock = mock(GalaxyCoreProxy.class);
        when(mainMock.getDatabaseConfiguration()).thenReturn(databaseConfiguration);

        try {
            I18N.init(mainMock);
        }catch (Exception e) {e.printStackTrace();}

        I18N.setDefaultByLang("de_DE", "TestKey1", "Test Value1");
        I18N.setDefaultByLang("de_DE", "TestKey2", "Test Value2");
        I18N.setDefaultByLang("de_DE", "TestKey3", "Test Value3");
        I18N.setDefaultByLang("de_DE", "TestKey4", "Test Value4");
        I18N.setDefaultByLang("de_DE", "TestKey5", "Test Value5");
        I18N.setDefaultByLang("de_DE", "TestKey6", "Test Value6");

        I18N.setDefaultByLang("fr_FR", "TestKey1", "t??st v??lue1");
        I18N.setDefaultByLang("fr_FR", "TestKey2", "t??st v??lue2");
        I18N.setDefaultByLang("fr_FR", "TestKey3", "t??st v??lue3");
        I18N.setDefaultByLang("fr_FR", "TestKey4", "t??st v??lue4");
        I18N.setDefaultByLang("fr_FR", "TestKey5", "t??st v??lue5");
        I18N.setDefaultByLang("fr_FR", "TestKey6", "t??st v??lue6");

        I18N.load();

        assertEquals(I18N.getByLang("de_DE", "TestKey1"), "Test Value1");
        assertEquals(I18N.getByLang("fr_FR", "TestKey2"), "t??st v??lue2");
    }

}
