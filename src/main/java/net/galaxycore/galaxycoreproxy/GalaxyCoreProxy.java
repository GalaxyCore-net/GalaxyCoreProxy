package net.galaxycore.galaxycoreproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.commands.BauserverCommand;
import net.galaxycore.galaxycoreproxy.commands.HelpCommand;
import net.galaxycore.galaxycoreproxy.commands.SendPerDHLCommand;
import net.galaxycore.galaxycoreproxy.commands.TeamCommand;
import net.galaxycore.galaxycoreproxy.configuration.ConfigNamespace;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.InternalConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.PrefixProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
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

    // COMMANDS //
    @Getter
    private HelpCommand helpCommand;
    @Getter
    private BauserverCommand bauserverCommand;
    @Getter
    private SendPerDHLCommand sendPerDHLCommand;
    @Getter
    private TeamCommand teamCommand;

    @Inject
    public GalaxyCoreProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        // CONFIGURATION //
        InternalConfiguration internalConfiguration = new InternalConfiguration(new File("plugins/GalaxyCoreProxy/"));
        databaseConfiguration = new DatabaseConfiguration(internalConfiguration);

        proxyNamespace = databaseConfiguration.getNamespace("proxy");

        proxyNamespace.setDefault("proxy.prefix", "§5GalaxyCore.net §7| §r");

        PrefixProvider.setPrefix(proxyNamespace.get("proxy.prefix"));

        // INTERNATIONALISATION //

        I18N.init(this);

        server.getScheduler().buildTask(this, I18N::load).schedule();

        I18N.setDefaultByLang("de_DE", "proxy.command.help", "§6Information\\n" +
                "§8» §e/hub §8| §7Verbinde dich zum Lobby-Server\\n" +
                "§8» §e/report §8| §7Reporte einen Spieler\\n" +
                "§8» §cTeamSpeak§8: §7GalaxyCore.net\\n" +
                "§8» §cDiscord§8: §7dc.GalaxyCore.net\\n" +
                "§8» §cWebsite§8: §7GalaxyCore.net\\n");

        I18N.setDefaultByLang("de_DE", "proxy.command.bauserver.int_required", "§cBitte gib eine ganze Zahl an!");

        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.wrong_usage", "§cBitte benutze §7/spd <Spieler> <Server>!");
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.target_not_found", "§cSpieler nicht gefunden!");
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.server_not_found", "§cServer nicht gefunden!");

        // BLOCK TAB COMPLETION //
        tabCompletionListener = new TabCompletionListener(this);

        // COMMANDS //
        helpCommand = new HelpCommand(this);
        bauserverCommand = new BauserverCommand(this);
        sendPerDHLCommand = new SendPerDHLCommand(this);
        teamCommand = new TeamCommand(this);

        logger.info("Loaded GalaxyCore-Proxy plugin");

    }

    public void registerListener(Object listener) {
        server.getEventManager().register(this, listener);
    }

    public void registerCommand(Command command, String name, String... aliases) {
        CommandMeta commandMeta = server.getCommandManager().metaBuilder(name).aliases(aliases).build();
        server.getCommandManager().register(commandMeta, command);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        databaseConfiguration.disable();

    }

}
