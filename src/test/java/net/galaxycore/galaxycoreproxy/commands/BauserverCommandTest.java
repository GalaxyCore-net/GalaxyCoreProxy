package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class BauserverCommandTest {

    @Test
    void getServerName() {

        String[] args = new String[] {"1"};

        GalaxyCoreProxy mockProxy = mock(GalaxyCoreProxy.class);

        CommandSource mockPlayer = mock(CommandSource.class);

        String expected = "Bauserver-1";
        String actual = new BauserverCommand().getBauserverServerName(args, mockPlayer);
        assertEquals(expected, actual);

    }

}
