package net.galaxycore.galaxycoreproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ConfigNamespace;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.InternalConfiguration;
import org.slf4j.Logger;

import java.io.File;

@Plugin(
        id = "galaxycoreproxy",
        name = "GalaxyCoreProxy",
        version = "1.0-SNAPSHOT",
        description = "Proxy Plugin for GalaxyCore.net",
        url = "https://galaxycore.net",
        authors = {"Flo-Mit-H"}
)
public class GalaxyCoreProxy {

    @Inject
    @Getter
    private Logger logger;

    // CONFIGURATION //
    @Getter
    private DatabaseConfiguration databaseConfiguration;
    private ConfigNamespace proxyNamespace;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        logger.info("Loaded GalaxyCore-Proxy plugin");

        // CONFIGURATION //
        InternalConfiguration internalConfiguration = new InternalConfiguration(new File("plugins/GalaxyCoreProxy/"));
        databaseConfiguration = new DatabaseConfiguration(internalConfiguration);

        proxyNamespace = databaseConfiguration.getNamespace("proxy");

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        databaseConfiguration.disable();

    }

}
