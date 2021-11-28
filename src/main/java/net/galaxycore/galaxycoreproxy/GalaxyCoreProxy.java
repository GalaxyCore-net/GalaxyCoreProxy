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
import net.galaxycore.galaxycoreproxy.listener.ChatListener;
import net.galaxycore.galaxycoreproxy.listener.PluginCommandListener;
import net.galaxycore.galaxycoreproxy.onlinetime.OnlineTime;
import net.galaxycore.galaxycoreproxy.onlinetime.OnlineTimeCommand;
import net.galaxycore.galaxycoreproxy.proxyPlayerControl.PlayerDisconnectListener;
import net.galaxycore.galaxycoreproxy.scheduler.BroadcastScheduler;
import net.galaxycore.galaxycoreproxy.tabcompletion.TabCompletionListener;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.io.File;

@Getter
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

    private final Logger logger;
    private final ProxyServer server;

    // CONFIGURATION //
    private DatabaseConfiguration databaseConfiguration;

    // API //
    @SuppressWarnings({"unused"})
    private ConfigNamespace proxyNamespace;

    // BLOCK TAB COMPLETION //
    private TabCompletionListener tabCompletionListener;

    // COMMANDS //
    private HelpCommand helpCommand;
    private BauserverCommand bauserverCommand;
    private SendPerDHLCommand sendPerDHLCommand;
    private TeamCommand teamCommand;
    private PluginCommand pluginCommand;
    private BroadcastCommand broadcastCommand;
    private JoinMeCommand joinMeCommand;
    private LoginCommand loginCommand;
    private LogoutCommand logoutCommand;
    private TeamChatCommand teamChatCommand;
    private AdminChatCommand adminChatCommand;
    private MSGCommand msgCommand;
    private RCommand rCommand;
    private MSGToggleCommand msgToggleCommand;
    private OnlineTimeCommand onlineTimeCommand;
    private CommandSpyCommand commandSpyCommand;
    private SocialSpyCommand socialSpyCommand;

    // LISTENER //
    private PluginCommandListener pluginCommandListener;
    private PlayerDisconnectListener playerDisconnectListener;

    // SCHEDULER //
    private BroadcastScheduler broadcastScheduler;

    // LUCKPERMS API //
    LuckPerms luckPermsAPI;

    // ONLINETIME //
    private OnlineTime onlineTime;

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
        proxyNamespace.setDefault("proxy.mute.default_reason", "1");
        proxyNamespace.setDefault("forbiddenWords", "affenarsch|affenkopf|analpirat|analtaucher|ankotzer|anpinkler|anscheißer|antänzer|arschlecker|arschloch|arschnase|arschtonne|arschwichser|ausschiss|blindschleiche|blödmann|chemieunfall|donnerfotze|dumpfbacke|dummarsch|dummie|dummschwätzer|ekelalfred|ekelpaket|fickeumel|fickfotze|fickfresse|fickmuschi|flachkopp|flaschwichser|fotze|fotzenlecker|fressmuschi|fresspilz|gammelkerl|gammler|gehirnpfeife|gehirnzecke|gummimuschi|hackfresse|hirnfresse|hodenkobold|holzkopf|idiot|kackbratze|kackebacke|kackfresse|kackhaufen|kackstelze|kackvogel|kobold|klugscheißer|knallfrosch|knalltüte|kothaufen|kotzbrocken|lackaffe|luftikuss|luftsack|pimmel|pimmellutscher|pimmelnase|pissnelke|pissnudel|sackgassenjunge|sackratte|scheißhaufen|scheißkerl|schimmelnarr|schissbold|schlappschwanz|schmierfink|schwätzer|schwanzsauger|stinkbolzen|stinkmumie|stinksack|terrorclown|teufelsanbeter|tonnenlecker|topffresse|topflappen|torfkopf|torfnase|torfnudel|wichser|wuchtbrumme|zicke|zuckerschlucker");

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
        I18N.setDefaultByLang("de_DE", "proxy.command.bauserver.int_required", "§cBitte gib eine ganze Zahl an!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.wrong_usage", "§cBitte benutze §7/spd <Spieler> <Server>!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.target_not_found", "§cSpieler nicht gefunden!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.server_not_found", "§cServer nicht gefunden!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.sendperdhl.server", "§aServer", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.team.team", "§aTeam", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.broadcast", "\n§6Broadcast: §4§l", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.noperms", "§fUnknown command. Type \"/help\" for help.", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.not_in_lobby", "§cDu kannst diesen Command nicht in der Lobby ausführen!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.joinme_not_found", "§cDieses JoinMe existiert nicht", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.click_to_join", "§cKlicke zum Beitreten", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.player_sent_joinme", "§6%player% §7hat ein JoinMe für §e%server% §7geschickt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.in_cooldown", "§cDu befindest dich noch im Cooldown", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.login.already_logged_in", "§cDu bist bereits eingeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.joinme_exists", "§cDieses JoinMe existiert bereits");
        I18N.setDefaultByLang("de_DE", "proxy.command.login.logged_in", "§aDu bist nun eingeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.plugins.no_permission", "§fProxyPlugins (9): §aHmm§f, §aich§f, §aglaube§f, §adass§f, §adu§f, §ahier§f, §anichts§f, §afinden§f, §awirst.", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.not_logged_in", "§cDu bist nicht eingeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.logged_out", "§aDu bist nun ausgeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.teamchat.prefix",  "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_player", "§cDu kannst diesen Spieler nicht bannen!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_yourself", "§cDu kannst dich nicht selber bannen!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.kick.player_404", "§cDer Spieler wurde nicht gefunden", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.kick.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!", true);

        I18N.setDefaultByLang("de_DE", "proxy.command.ban.not_a_number", "§cDies ist keine ganze Zahl!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_ban%", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.usage", "§cBitte nutze §e/msg <Spieler> <Nachricht>", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.player_not_found", "§cDieser Spieler wurde nicht gefunden", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.transmission", "§e{p1} §6-> §e{p2}§e: §7{msg}", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.you", "Du");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.noperms", "§cDu hast hierfür keine Rechte", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.locked", "§cDu darfst diesem Spieler keine Nachrichten senden", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.r.notfound", "§cDu hast in letzter Zeit niemandem eine private Nachricht geschrieben. Du kannst niemandem antworten (>_<)", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msgtoggle.no_permissions", "§cDu hast hierfür keine Rechte", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msgtoggle.on", "§7Deine privaten Nachrichten sind jetzt §cgeschlossen§7.", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.msgtoggle.off", "§7Deine privaten Nachrichten sind jetzt §eoffen§7.", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.onlinetime", "§7Deine OnlineZeit ist§e %h% h %m% m", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.onlinetime.other", "§e%player%'s OnlineZeit ist %h% h %m% m", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.onlinetime.player404", "§cDieser Spieler wurde nicht gefunden", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.socialspy.no_permissions", "§cDu hast hierfür keine Rechte", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.socialspy.on", "§7SocialSpy §aaktiviert", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.socialspy.off", "§7SocialSpy §cdeaktiviert", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.no_permissions", "§cDu hast hierfür keine Rechte", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.on", "§7CommandSpy §aaktiviert", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.off", "§7CommandSpy §cdeaktiviert", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.spy", "§e{player} verwendete soeben /{cmd}", true);
        I18N.setDefaultByLang("de_DE", "proxy.default_kick_reason", "§cVerbindung zum Server verloren", true);
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.banscreen_text", "Du wurdest von einem Teammitglied gebannt", true);
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.kickscreen_text", "§cDu wurdest von einem Teammitglied gekickt\n§aGrund: §f%reason%", true);

        I18N.setDefaultByLang("de_DE", "proxy.ban.banlog_entry", "§c« §f{action} §c»\n" +
                "Spieler: {player}\n" +
                "Grund: {reason}\n" +
                "Bannpunkte: {banPoints}\n" +
                "Von: {from}\n" +
                "Bis: {until}\n" +
                "Permanent: {permanent}\n" +
                "Staff: {staff}");

        I18N.setDefaultByLang("de_DE", "proxy.ban.muted.chat", "§cDu wurdest von einem Teammitglied gemutet, also kannst du im moment nicht schreiben!\n" +
                "§cWenn du denkst, dass ein Fehler vorliegt, kontaktiere bitte den Support!");

        I18N.setDefaultByLang("de_DE", "proxy.command.mute.cant_mute_yourself", "§cDu kannst dich nicht selbst muten!");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.mute.message", "Nachricht");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.anti_vpn", "§cBitte schalte deine VPN/deinen Proxy aus, um auf diesem Server zu spielen");
        I18N.setDefaultByLang("de_DE", "proxy.command.ip.usage", "§cBenutzung: §f/ip <player>", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.ip.ip_of_player", "§cIp von Spieler {player}: {ip}", true);

        // English Messages
        I18N.setDefaultByLang("en_GB", "proxy.command.help", "§6Information\n" +
                "§8» §e/hub §8| §7connect to the Lobby-Server\n" +
                "§8» §e/report §8| §7Report a Player\n" +
                "§8» §cTeamSpeak§8: §7GalaxyCore.net\n" +
                "§8» §cDiscord§8: §7dc.GalaxyCore.net\n" +
                "§8» §cWebsite§8: §7GalaxyCore.net\n");
        I18N.setDefaultByLang("en_GB", "proxy.command.bauserver.int_required", "§cPlease provide a valid integer!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.wrong_usage", "§cPlease use §7/spd <player> <reason>!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.target_not_found", "§cPlayer not found!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.server_not_found", "§cServer not found!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.sendperdhl.server", "§aServer", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.team.team", "§aTeam", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.broadcast", "\n§6Broadcast: §4§l", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.noperms", "§fUnknown command. Type \"/help\" for help.", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.not_in_lobby", "§cYou can´t execute this command in the lobby!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.joinme_not_found", "§cThis JoinMe doesn´t exist", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.joinme_exists", "§cThis JoinMe already exists", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.click_to_join", "§cClick to Join", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.player_sent_joinme", "§6%player% §7sent a Joinme for §e%server%", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.in_cooldown", "§cYou are still in Cooldown", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.login.already_logged_in", "§cYou´re logged in already", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.login.logged_in", "§aYou´re logged in now", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.plugins.no_permission", "§eProxyPlugins (9): §aHmm§f, §aI§f, §athink§f, §athat§f, §ayou§f, §aown´t§f, §afind§f, §aanything§f, §ahere.", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.logout.not_logged_in", "§cYou´re not logged in", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.logout.logged_out", "§aYou´re logged out now", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.teamchat.prefix", "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ", true);
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.banscreen_text", "You were banned by a Staff Member", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.too_few_args", "§cPlease use §7/ban <player> [reason]§c!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.cant_ban_player", "§cYou can´t ban this Player!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.cant_ban_yourself", "§cYou can´t ban yourself!", true);
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.kickscreen_text", "§cYou were kicked by a Staff Member\n§aReason: §f%reason%", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.kick.player_404", "§cThis Player was not found", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.kick.too_few_args", "§cPlease use §7/ban <player> [reason]§c!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.not_a_number", "§cThis is not a valid number!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_ban%", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.usage", "§cPlease use §e/msg <Player> <Message>", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.player_not_found", "§cThis Player was not found", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.transmission", "§e{p1} §6-> §e{p2}§e: §7{msg}", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.you", "You");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.noperms", "§cYou do not have enough permissions to use this Command", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.locked", "§cYou're not allowed to send a message to this person", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.r.notfound", "§cYou didn't send any private messages lately", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.no_permissions", "§cYou do not have enough permissions to use this Command", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.on", "§cNobody §ecan message you now", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.off", "§aEveryone §ecan message you now", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.onlinetime", "§eYour OnlineTime is %h% hours and %m% minutes", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.onlinetime.other", "§e%player%'s OnlineTime is %h% hours and %m% minutes", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.onlinetime.player404", "§cThis Player was not found", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.off", "§eEveryone §ecan message you now", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.socialspy.no_permissions", "§cYou do not have enough permissions to use this Command", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.socialspy.on", "§7You now §acan§7 see the private Messages of others", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.socialspy.off", "§7You now §ccan't§7 see the private Messages of others", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.no_permissions", "§cYou do not have enough permissions to use this Command", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.on", "§7You now §acan§7 see the commands of others", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.off", "§7You now §ccan't§7 see the commands of others", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.spy", "§e{player} executed /{cmd}", true);
        I18N.setDefaultByLang("en_GB", "proxy.default_kick_reason", "§cYou got disconnected from the Server", true);

        I18N.setDefaultByLang("en_GB", "proxy.ban.banlog_entry", "§c« §f{action} §c»\n" +
                "player: {player}\n" +
                "Reason: {reason}\n" +
                "Banpoints: {banPoints}\n" +
                "From: {from}\n" +
                "Until: {until}\n" +
                "Permanent: {permanent}\n" +
                "Staff: {staff}");

        I18N.setDefaultByLang("en_GB", "proxy.ban.muted.chat", "§cYou were muted by a staff member, so you can´t chat at the moment!\n" +
                "§cIf you think this is wrong, please contact the Support!");

        I18N.setDefaultByLang("en_GB", "proxy.command.mute.cant_mute_yourself", "§cYou can´t mute yourself!", true);
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.mute.message", "Message");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.anti_vpn", "§cPlease turn of your VPN/Proxy to play on this server");
        I18N.setDefaultByLang("en_GB", "proxy.command.ip.usage", "§cUsage: §f/ip <player>", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ip.ip_of_player", "§cIp of Player {player}: {ip}", true);

        I18N.load();

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
        msgCommand = new MSGCommand();
        rCommand = new RCommand();
        msgToggleCommand = new MSGToggleCommand();
        onlineTimeCommand = new OnlineTimeCommand();
        socialSpyCommand = new SocialSpyCommand();
        commandSpyCommand = new CommandSpyCommand();

        // LISTENERS //
        pluginCommandListener = new PluginCommandListener();

        playerDisconnectListener = new PlayerDisconnectListener();

        new ChatListener();

        // SCHEDULER //
        broadcastScheduler = new BroadcastScheduler();

        // BAN SYSTEM //
        BanSystemProvider.setBanSystem(new BanSystem());

        // ONLINE TIME //
        onlineTime = new OnlineTime(this.getServer());

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
