package net.galaxycore.galaxycoreproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.commands.*;
import net.galaxycore.galaxycoreproxy.configuration.*;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18NPlayerLoader;
import net.galaxycore.galaxycoreproxy.joinme.JoinMeCommand;
import net.galaxycore.galaxycoreproxy.listener.PluginCommandListener;
import net.galaxycore.galaxycoreproxy.scheduler.BroadcastScheduler;
import net.galaxycore.galaxycoreproxy.tabcompletion.TabCompletionListener;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.io.File;

@Plugin(
        id = "galaxycoreproxy",
        name = "GalaxyCoreProxy",
        version = "1.0-SNAPSHOT",
        description = "Proxy Plugin for GalaxyCore.net",
        url = "https://galaxycore.net",
        authors = {"Flo-Mit-H"},
        dependencies = {
                @Dependency(id = "luckperms")
        }
)
public class GalaxyCoreProxy {

    @Getter
    private final Logger logger;

    @Getter
    private final ProxyServer server;

    // CONFIGURATION //
    @Getter
    private DatabaseConfiguration databaseConfiguration;
    @SuppressWarnings({"unused"})
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
    @Getter
    private PluginCommand pluginCommand;
    @Getter
    private BroadcastCommand broadcastCommand;
    @Getter
    private JoinMeCommand joinMeCommand;
    @Getter
    private LoginCommand loginCommand;
    @Getter
    private LogoutCommand logoutCommand;
    @Getter
    private TeamChatCommand teamChatCommand;
    @Getter
    private AdminChatCommand adminChatCommand;

    // LISTENER //
    @Getter
    private PluginCommandListener pluginCommandListener;

    // SCHEDULER //
    @Getter
    private BroadcastScheduler broadcastScheduler;

    // LUCKPERMS API //
    @Getter
    LuckPerms luckPermsAPI;

    @Inject
    public GalaxyCoreProxy(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        ProxyProvider.setProxy(this);

        // CONFIGURATION //
        InternalConfiguration internalConfiguration = new InternalConfiguration(new File("plugins/GalaxyCoreProxy/"));
        databaseConfiguration = new DatabaseConfiguration(internalConfiguration);

        proxyNamespace = databaseConfiguration.getNamespace("proxy");

        proxyNamespace.setDefault("global.prefix", "§5GalaxyCore.net §7| §r");

        proxyNamespace.setDefault("proxy.broadcast.delay", "1m");

        proxyNamespace.setDefault("proxy.joinme.delay", "3m");
        proxyNamespace.setDefault("proxy.joinme.cooldown", "1h");

        PrefixProvider.setPrefix(proxyNamespace.get("proxy.prefix"));

        // INTERNATIONALISATION //

        I18N.init(this);

        I18NPlayerLoader.setPlayerloaderInstance(new I18NPlayerLoader(this));

        server.getScheduler().buildTask(this, I18N::load).schedule();

        I18N.setDefaultByLang("de_DE", "proxy.command.help", "§6Information\n" +
                "§8» §e/hub §8| §7Verbinde dich zum Lobby-Server\n" +
                "§8» §e/report §8| §7Reporte einen Spieler\n" +
                "§8» §cTeamSpeak§8: §7GalaxyCore.net\n" +
                "§8» §cDiscord§8: §7dc.GalaxyCore.net\n" +
                "§8» §cWebsite§8: §7GalaxyCore.net\n");

        I18N.setDefaultByLang("de_DE", "proxy.command.bauserver.int_required", "§cBitte gib eine ganze Zahl an!");

        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.wrong_usage", "§cBitte benutze §7/spd <Spieler> <Server>!");
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.target_not_found", "§cSpieler nicht gefunden!");
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.server_not_found", "§cServer nicht gefunden!");
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.server", "§aServer");

        I18N.setDefaultByLang("de_DE", "proxy.command.team.team", "§aTeam");

        I18N.setDefaultByLang("de_DE", "proxy.command.braodcast", "\n§6Broadcast: §4§l");

        I18N.setDefaultByLang("de_DE", "proxy.scheduler.broadcast", "§6Folge uns doch auf Twitter: https://twitter.com/Galaxycore_net");

        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.noperms", "§fUnknown command. Type \"/help\" for help.");
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.not_in_lobby", "§cDu kannst diesen Command nicht in der Lobby ausführen!");
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.joinme_not_found", "§cDieses JoinMe existiert nicht");
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.click_to_join", "§cKlicke zum Beitreten");
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.player_sent_joinme", "§6%player% §7hat ein JoinMe für §e%server% §7geschickt");
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.in_cooldown", "§cDu befindest dich noch im Cooldown");

        I18N.setDefaultByLang("de_DE", "proxy.command.login.already_logged_in", "§cDu bist bereits eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.login.logged_in", "§aDu bist nun eingeloggt");

        I18N.setDefaultByLang("de_DE", "proxy.command.plugins.no_permission", "§aHmm§f, §aich§f, §aglaube§f, §adass§f, §adu§f, §ahier§f, §anichts§f, §afinden§f, §awirst.");

        I18N.setDefaultByLang("de_DE", "proxy.command.logout.not_logged_in", "§cDu bist nicht eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.logged_out", "§aDu bist nun ausgeloggt");

        // LUCKPERMS API //
        luckPermsAPI = LuckPermsProvider.get();

        // BLOCK TAB COMPLETION //
        tabCompletionListener = new TabCompletionListener();

        // COMMANDS //
        helpCommand = new HelpCommand();
        bauserverCommand = new BauserverCommand();
        sendPerDHLCommand = new SendPerDHLCommand();
        teamCommand = new TeamCommand();
        pluginCommand = new PluginCommand();
        broadcastCommand = new BroadcastCommand();
        joinMeCommand = new JoinMeCommand();
        loginCommand = new LoginCommand();
        logoutCommand = new LogoutCommand();
        teamChatCommand = new TeamChatCommand();
        adminChatCommand = new AdminChatCommand();

        // LISTENERS //
        pluginCommandListener = new PluginCommandListener();

        // SCHEDULER //
        broadcastScheduler = new BroadcastScheduler();

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
