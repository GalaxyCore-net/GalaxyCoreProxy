package net.galaxycore.galaxycoreproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ConfigNamespace;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.InternalConfiguration;
import net.galaxycore.galaxycoreproxy.tabcompletion.TabCompletionListener;
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

    @Getter
    private final Logger logger;

    @Getter
    private final ProxyServer server;

    // CONFIGURATION //
    @Getter
    private DatabaseConfiguration databaseConfiguration;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Getter
    // API
    private ConfigNamespace proxyNamespace;

    // BLOCK TAB COMPLETION //
    @Getter
    private TabCompletionListener tabCompletionListener;

    @Inject
    public GalaxyCoreProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        logger.info("Loaded GalaxyCore-Proxy plugin");

        // CONFIGURATION //
        InternalConfiguration internalConfiguration = new InternalConfiguration(new File("plugins/GalaxyCoreProxy/"));
        databaseConfiguration = new DatabaseConfiguration(internalConfiguration);

        proxyNamespace = databaseConfiguration.getNamespace("proxy");

        // BLOCK TAB COMPLETION //
        tabCompletionListener = new TabCompletionListener(this);

    }

    public void registerListener(Object listener) {
        server.getEventManager().register(this, listener);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        databaseConfiguration.disable();

    }

}
