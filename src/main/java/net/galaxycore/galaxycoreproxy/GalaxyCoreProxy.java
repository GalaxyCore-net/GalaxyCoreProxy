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
        proxyNamespace.setDefault("proxy.command.kick.default_reason", "Fehlverhalten");
        proxyNamespace.setDefault("proxy.mute.default_reason", "1");
        proxyNamespace.setDefault("proxy.command.report.default_reason", "Fehlverhalten");

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
        I18N.setDefaultByLang("de_DE", "proxy.command.joinme.joinme_exists", "§cDieses JoinMe existiert bereits");
        I18N.setDefaultByLang("de_DE", "proxy.command.login.already_logged_in", "§cDu bist bereits eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.login.logged_in", "§aDu bist nun eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.plugins.no_permission", "§aHmm§f, §aich§f, §aglaube§f, §adass§f, §adu§f, §ahier§f, §anichts§f, §afinden§f, §awirst.");
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.not_logged_in", "§cDu bist nicht eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.logged_out", "§aDu bist nun ausgeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("de_DE", "proxy.command.teamchat.prefix",  "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_player", "§cDu kannst diesen Spieler nicht bannen!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_yourself", "§cDu kannst dich nicht selber bannen!");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.kickscreen_text", "§cDu wurdest von einem Teammitglied gekickt\n§aGrund: §f%reason%");
        I18N.setDefaultByLang("de_DE", "proxy.player_404", "§cDer Spieler wurde nicht gefunden");
        I18N.setDefaultByLang("de_DE", "proxy.command.kick.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.not_a_number", "§cDies ist keine ganze Zahl!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_ban%");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.usage", "§cBitte nutze §e/msg <Spieler> <Nachricht>");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.player_not_found", "§cDieser Spieler wurde nicht gefunden");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.transmission", "§e{p1} §6-> §e{p2}§e: §7{msg}");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.you", "Du");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.noperms", "§cDu hast hierfür keine Rechte");
        I18N.setDefaultByLang("de_DE", "proxy.command.msg.locked", "§cDu darfst diesem Spieler keine Nachrichten senden");
        I18N.setDefaultByLang("de_DE", "proxy.command.r.notfound", "§cDu hast in letzter Zeit niemandem eine private Nachricht geschrieben. Du kannst niemandem antworten (>_<)");
        I18N.setDefaultByLang("de_DE", "proxy.command.msgtoggle.no_permissions", "§cDu hast hierfür keine Rechte");
        I18N.setDefaultByLang("de_DE", "proxy.command.msgtoggle.on", "§7Deine privaten Nachrichten sind jetzt §cgeschlossen§7.");
        I18N.setDefaultByLang("de_DE", "proxy.command.msgtoggle.off", "§7Deine privaten Nachrichten sind jetzt §eoffen§7.");
        I18N.setDefaultByLang("de_DE", "proxy.command.onlinetime", "§7Deine OnlineZeit ist§e %h% h %m% m");
        I18N.setDefaultByLang("de_DE", "proxy.command.onlinetime.other", "§e%player%'s OnlineZeit ist %h% h %m% m");
        I18N.setDefaultByLang("de_DE", "proxy.command.onlinetime.player404", "§cDieser Spieler wurde nicht gefunden");
        I18N.setDefaultByLang("de_DE", "proxy.command.socialspy.no_permissions", "§cDu hast hierfür keine Rechte");
        I18N.setDefaultByLang("de_DE", "proxy.command.socialspy.on", "§7SocialSpy §aaktiviert");
        I18N.setDefaultByLang("de_DE", "proxy.command.socialspy.off", "§7SocialSpy §cdeaktiviert");
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.no_permissions", "§cDu hast hierfür keine Rechte");
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.on", "§7CommandSpy §aaktiviert");
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.off", "§7CommandSpy §cdeaktiviert");
        I18N.setDefaultByLang("de_DE", "proxy.command.commandspy.spy", "§e{player} verwendete soeben /{cmd}");
        I18N.setDefaultByLang("de_DE", "proxy.default_kick_reason", "§cVerbindung zum Server verloren");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.banscreen_text", "Du wurdest von einem Teammitglied gebannt");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.kickscreen_text", "§cDu wurdest von einem Teammitglied gekickt\n§aGrund: §f%reason%");

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
        I18N.setDefaultByLang("de_DE", "proxy.command.mute.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_mute%");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.anti_vpn", "§cBitte schalte deine VPN/deinen Proxy aus, um auf diesem Server zu spielen");
        I18N.setDefaultByLang("de_DE", "proxy.command.ip.usage", "§cBenutzung: §f/ip <spieler>");
        I18N.setDefaultByLang("de_DE", "proxy.command.ip.ip_of_player", "§cIp von Spieler {player}: {ip}");
        I18N.setDefaultByLang("de_DE", "proxy.command.playerinfo.usage", "§cBenutzung: §f/playerinfo <spieler>");
        I18N.setDefaultByLang("de_DE", "proxy.command.playerinfo.info_for_player", "§8» §7Name§8: §c{name} §8(§aOnline§8)\n" +
                "§8» §7UUID§8: §c{uuid}\n" +
                "§8» §7ID§8: §c{id}\n" +
                "§8» §7Erster Login§8: §c{first_login}\n" +
                "§8» §7Server§8: §c{server}\n" +
                "§8» §7Onlinezeit§8: §c{onlinetime}\n" +
                "§8» §7Gebannt§8: §c{banned}\n" +
                "§8» §7Gemutet§8: §c{muted}\n" +
                "§8» §7Punkte§8: §c{ban_points} §8 {mute_points} | §8{warn_points}\n" +
                "§8» §7Bans§8: §c{bans}\n" +
                "§8» §7Mutes§8: §c{mutes}\n" +
                "§8» §7Warnungen§8: §c{warns}\n");
        I18N.setDefaultByLang("de_DE", "proxy.command.unmute.usage", "§cBenutzung: §f/unmute <spieler>");
        I18N.setDefaultByLang("de_DE", "proxy.command.history.usage", "§cBenutzung: §f/history <spieler>");
        I18N.setDefaultByLang("de_DE", "proxy.command.history.begin", "§cHistorie von §a{player}§c:");
        I18N.setDefaultByLang("de_DE", "proxy.command.history.entry", "§a{id} | §c{action} | §e{reason} | §8{staff} | §b{date}");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.usage", "§cBenutzung: §f/report <spieler>");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.cant_report_player", "§cDu kannst diesen Spieler nicht melden!");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.cant_report_yourself", "§cDu kannst dich nicht selber melden!");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.player_already_reported", "§cDieser Spieler ist bereits gemeldet");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.player_not_reported", "§cDieser Spieler wurde noch nicht gemeldet");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.report_denied", "§cDeine Meldung wurde abgelehnt");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.report_claimed", "§cDu hast diese Meldung angenommen");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.accept", "§aAnnehmen");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.deny", "§cAblehnen");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.only_claimed_staff", "§cDiese Aktion kann nur von dem Teammitglied ausgeführt werden, welches diese Meldung angenommen hat!");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.report_clossed", "§cDiese Meldung wurde geschlossen");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.no_permission", "§cDu hast hierfür keine Rechte!");
        I18N.setDefaultByLang("de_DE", "proxy.command.report.this_report_was_denied", "§cDieser Report wurde geschlossen");

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
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.joinme_exists", "§cThis JoinMe already exists");
        I18N.setDefaultByLang("en_GB", "proxy.command.login.already_logged_in", "§cYou´re logged in already");
        I18N.setDefaultByLang("en_GB", "proxy.command.login.logged_in", "§aYou´re logged in now");
        I18N.setDefaultByLang("en_GB", "proxy.command.plugins.no_permission", "§aHmm§f, §aI§f, §athink§f, §athat§f, §ayou§f, §aown´t§f, §afind§f, §aanything§f, §ahere.");
        I18N.setDefaultByLang("en_GB", "proxy.command.logout.not_logged_in", "§cYou´re not logged in");
        I18N.setDefaultByLang("en_GB", "proxy.command.logout.logged_out", "§aYou´re logged out now");
        I18N.setDefaultByLang("en_GB", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("en_GB", "proxy.command.teamchat.prefix", "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.banscreen_text", "You were banned by a Staff Member");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.too_few_args", "§cPlease use §7/ban <player> [reason]§c!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.cant_ban_player", "§cYou can´t ban this Player!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.cant_ban_yourself", "§cYou can´t ban yourself!");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.kickscreen_text", "§cYou were kicked by a Staff Member\n§aReason: §f%reason%");
        I18N.setDefaultByLang("en_GB", "proxy.player_404", "§cThis Player was not found");
        I18N.setDefaultByLang("en_GB", "proxy.command.kick.too_few_args", "§cPlease use §7/ban <player> [reason]§c!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.not_a_number", "§cThis is not a valid number!");
        I18N.setDefaultByLang("en_GB", "proxy.command.ban.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_ban%");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.usage", "§cPlease use §e/msg <Player> <Message>");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.player_not_found", "§cThis Player was not found");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.transmission", "§e{p1} §6-> §e{p2}§e: §7{msg}");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.you", "You");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.noperms", "§cYou do not have enough permissions to use this Command");
        I18N.setDefaultByLang("en_GB", "proxy.command.msg.locked", "§cYou're not allowed to send a message to this person");
        I18N.setDefaultByLang("en_GB", "proxy.command.r.notfound", "§cYou didn't send any private messages lately");
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.no_permissions", "§cYou do not have enough permissions to use this Command");
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.on", "§cNobody §ecan message you now");
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.off", "§aEveryone §ecan message you now");
        I18N.setDefaultByLang("en_GB", "proxy.command.onlinetime", "§eYour OnlineTime is %h% hours and %m% minutes");
        I18N.setDefaultByLang("en_GB", "proxy.command.onlinetime.other", "§e%player%'s OnlineTime is %h% hours and %m% minutes");
        I18N.setDefaultByLang("en_GB", "proxy.command.onlinetime.player404", "§cThis Player was not found");
        I18N.setDefaultByLang("en_GB", "proxy.command.msgtoggle.off", "§eEveryone §ecan message you now");
        I18N.setDefaultByLang("en_GB", "proxy.command.socialspy.no_permissions", "§cYou do not have enough permissions to use this Command");
        I18N.setDefaultByLang("en_GB", "proxy.command.socialspy.on", "§7You now §acan§7 see the private Messages of others");
        I18N.setDefaultByLang("en_GB", "proxy.command.socialspy.off", "§7You now §ccan't§7 see the private Messages of others");
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.no_permissions", "§cYou do not have enough permissions to use this Command");
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.on", "§7You now §acan§7 see the commands of others");
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.off", "§7You now §ccan't§7 see the commands of others");
        I18N.setDefaultByLang("en_GB", "proxy.command.commandspy.spy", "§e{player} executed /{cmd}");
        I18N.setDefaultByLang("en_GB", "proxy.default_kick_reason", "§cYou got disconnected from the Server");

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

        I18N.setDefaultByLang("en_GB", "proxy.command.mute.cant_mute_yourself", "§cYou can´t mute yourself!");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.mute.message", "Message");
        I18N.setDefaultByLang("en_GB", "proxy.command.mute.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_mute%");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.anti_vpn", "§cPlease turn of your VPN/Proxy to play on this server");
        I18N.setDefaultByLang("en_GB", "proxy.command.ip.usage", "§cUsage: §f/ip <player>");
        I18N.setDefaultByLang("en_GB", "proxy.command.ip.ip_of_player", "§cIp of Player {player}: {ip}");
        I18N.setDefaultByLang("en_GB", "proxy.command.playerinfo.usage", "§cUsage: §f/playerinfo <player>");
        I18N.setDefaultByLang("en_GB", "proxy.command.playerinfo.info_for_player", "§8» §7name§8: §c{name} §8(§aOnline§8)\n" +
                "§8» §7UUID§8: §c{uuid}\n" +
                "§8» §7ID§8: §c{id}\n" +
                "§8» §7first login§8: §c{first_login}\n" +
                "§8» §7server§8: §c{server}\n" +
                "§8» §7onlinetime§8: §c{onlinetime}\n" +
                "§8» §7banned§8: §c{banned}\n" +
                "§8» §7muted§8: §c{muted}\n" +
                "§8» §7points§8: §c{ban_points} §8 {mute_points} | §8{warn_points}\n" +
                "§8» §7bans§8: §c{bans}\n" +
                "§8» §7mutes§8: §c{mutes}\n" +
                "§8» §7warns§8: §c{warns}\n");
        I18N.setDefaultByLang("en_GB", "proxy.command.unmute.usage", "§cUsage: §f/unmute <player>");
        I18N.setDefaultByLang("en_GB", "proxy.command.history.usage", "§cUsage: §f/history <player>");
        I18N.setDefaultByLang("en_GB", "proxy.command.history.begin", "§cHistory of §a{player}§c:");
        I18N.setDefaultByLang("en_GB", "proxy.command.history.entry", "§a{id} §f| §c{action} §f| §e{reason} §f| §8{staff} §f| §b{date}");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.usage", "§cUsage: §f/report <player>");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.cant_report_player", "§cYou can´t report this player!");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.cant_report_yourself", "§cYou can´t report yourself!");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.player_already_reported", "§cThis player is already reported");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.player_not_reported", "§cThis player is not reported");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.report_denied", "§cYour report was denied");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.report_claimed", "§cYou claimed this report");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.accept", "§aAccept");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.deny", "§cDeny");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.only_claimed_staff", "§cOnly the Staff that claimed this report can perform this action!");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.report_closed", "§cThis report was closed");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.no_permission", "§cYou do not have enough permissions to use this command");
        I18N.setDefaultByLang("en_GB", "proxy.command.report.this_report_was_denied", "§cThis report was denied");

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
