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
import net.galaxycore.galaxycoreproxy.bansystem.BanSystem;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.commands.*;
import net.galaxycore.galaxycoreproxy.configuration.*;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18NPlayerLoader;
import net.galaxycore.galaxycoreproxy.joinme.JoinMeCommand;
import net.galaxycore.galaxycoreproxy.listener.PluginCommandListener;
import net.galaxycore.galaxycoreproxy.proxyPlayerControl.PlayerDisconnectListener;
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
    @Getter
    private PlayerDisconnectListener playerDisconnectListener;

    // SCHEDULER //
    @Getter
    private BroadcastScheduler broadcastScheduler;

    // LUCKPERMS API //
    @Getter
    LuckPerms luckPermsAPI;
//
//    // BAN SYSTEM //
//    @Getter
//    private BanSystem banSystem;

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

        proxyNamespace.setDefault("proxy.joinme.delay", "3m");
        proxyNamespace.setDefault("proxy.joinme.cooldown", "1h");

        proxyNamespace.setDefault("proxy.bansystem.banlog_webhook", "https://discord.com/api/webhooks/882263428591419442/eTztbTcJ5TvZMJJhLC5Q__dTqwLHe91ryfL5TGdmOhdNRj_j47N4GMeMwIguM15syQ1M");
        proxyNamespace.setDefault("proxy.bansystem.banscreen_url", "https://galaxycore.net/unban");
        proxyNamespace.setDefault("proxy.bansystem.banscreen_text", "You were banned by a Staff Member");
        proxyNamespace.setDefault("proxy.ban.default_reason", "1");
        proxyNamespace.setDefault("proxy.commnad.kick.default_reason", "Fehlverhalten");

        PrefixProvider.setPrefix(proxyNamespace.get("global.prefix"));
        proxyNamespace.setDefault("proxy.commandblacklist", "chattools|velocity");

        // INTERNATIONALISATION //

        I18N.init(this);

        I18NPlayerLoader.setPlayerloaderInstance(new I18NPlayerLoader(this));

        // German Messages
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

        I18N.setDefaultByLang("de_DE", "proxy.command.broadcast", "\n§6Broadcast: §4§l");

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

        I18N.setDefaultByLang("de_DE", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("de_DE", "proxy.command.teamchat.prefix",  "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ");

        I18N.setDefaultByLang("de_DE", "proxy.bansystem.banscreen_text", "Du wurdest von einem Teammitglied gebannt");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_player", "§cDu kannst diesen Spieler nicht bannen!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_yourself", "§cDu kannst dich nicht selber bannen!");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.kickscreen_text", "§cDu wurdest von einem Teammitglied gekickt\n§aGrund: §f%reason%");
        I18N.setDefaultByLang("de_DE", "proxy.command.kick.player_404", "§cDer Spieler wurde nicht gefunden");
        I18N.setDefaultByLang("de_DE", "proxy.command.kick.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.not_a_number", "§cDies ist keine ganze Zahl!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_ban%");
        I18N.setDefaultByLang("de_DE", "proxy.default_kick_reason", "§cVerbindung zum Server verloren");

        I18N.setDefaultByLang("de_DE", "proxy.ban.banlog_entry", "§c« §f{action} §c»\n" +
                "Spieler: {player}\n" +
                "Grund: {reason}\n" +
                "Bannpunkte: {banPoints}\n" +
                "Von: {from}\n" +
                "Bis: {until}\n" +
                "Permanent: {permanent}\n" +
                "Staff: {staff}");

        // English Messages
        I18N.setDefaultByLang("en_GB", "proxy.command.help", "§6Information\n" +
                "§8» §e/hub §8| §7connect to the Lobby-Server\n" +
                "§8» §e/report §8| §7Report a Player\n" +
                "§8» §cTeamSpeak§8: §7GalaxyCore.net\n" +
                "§8» §cDiscord§8: §7dc.GalaxyCore.net\n" +
                "§8» §cWebsite§8: §7GalaxyCore.net\n");

        I18N.setDefaultByLang("en_GB", "proxy.command.bauserver.int_required", "§cPlease provide a valid integer!");

        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.wrong_usage", "§cPlease use §7/spd <player> <reason>!");
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.target_not_found", "§cPlayer not found!");
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.server_not_found", "§cServer not found!");
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.server", "§aServer");

        I18N.setDefaultByLang("en_GB", "proxy.command.team.team", "§aTeam");

        I18N.setDefaultByLang("en_GB", "proxy.command.broadcast", "\n§6Broadcast: §4§l");

        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.noperms", "§fUnknown command. Type \"/help\" for help.");
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.not_in_lobby", "§cYou can´t execute this command in the lobby!");
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.joinme_not_found", "§cThis JounMe doesn´t exist");
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.click_to_join", "§cClick to Join");
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.player_sent_joinme", "§6%player% §7sent a Joinme for §e%server%");
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.in_cooldown", "§cYou are still in Cooldown");

        I18N.setDefaultByLang("en_GB", "proxy.command.login.already_logged_in", "§cYou´re logged in already");
        I18N.setDefaultByLang("en_GB", "proxy.command.login.logged_in", "§aYou´re logged in now");

        I18N.setDefaultByLang("en_GB", "proxy.command.plugins.no_permission", "§aHmm§f, §aI§f, §athink§f, §athat§f, §ayou§f, §aown´t§f, §afind§f, §aanything§f, §ahere.");

        I18N.setDefaultByLang("en_GB", "proxy.command.logout.not_logged_in", "§cYou´re not logged in");
        I18N.setDefaultByLang("en_GB", "proxy.command.logout.logged_out", "§aYou´re logged out now");

        I18N.setDefaultByLang("en_GB", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("en_GB", "proxy.command.teamchat.prefix", "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ");

        I18N.load();

        I18N.setDefaultByLang("en_GB", "proxy.bansystem.banscreen_text", "You were banned by a Staff Member");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.too_few_args", "§cPlease use §7/ban <player> [reason]§c!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.cant_ban_player", "§cYou can´t ban this Player!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.cant_ban_yourself", "§cYou can´t ban yourself!");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.kickscreen_text", "§cYou were kicked by a Staff Member\n§aReason: §f%reason%");
        I18N.setDefaultByLang("en_GB", "proxy.command.kick.player_404", "§cThis Player was not found");
        I18N.setDefaultByLang("en_GB", "proxy.command.kick.too_few_args", "§cPlease use §7/ban <player> [reason]§c!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.not_a_number", "§cThis is not a valid number!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_ban%");

        I18N.setDefaultByLang("en_GB", "proxy.default_kick_reason", "§cYou got disconnected from the Server");

        I18N.setDefaultByLang("en_GB", "proxy.ban.banlog_entry", "§c« §f{action} §c»\n" +
                "player: {player}\n" +
                "Reason: {reason}\n" +
                "Banpoints: {banPoints}\n" +
                "From: {from}\n" +
                "Until: {until}\n" +
                "Permanent: {permanent}\n" +
                "Staff: {staff}");

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

        playerDisconnectListener = new PlayerDisconnectListener();

        // SCHEDULER //
        broadcastScheduler = new BroadcastScheduler();

        // BAN SYSTEM //
        BanSystemProvider.setBanSystem(new BanSystem());

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
