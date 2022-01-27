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
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystem;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.commands.*;
import net.galaxycore.galaxycoreproxy.configuration.*;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18NPlayerLoader;
import net.galaxycore.galaxycoreproxy.friends.FriendCommand;
import net.galaxycore.galaxycoreproxy.friends.FriendManager;
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

    // Friend //
    private FriendManager friendManager;

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
    private FriendCommand friendCommand;

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
        proxyNamespace.setDefault("proxy.ban.default_reason", "1");
        proxyNamespace.setDefault("proxy.command.kick.default_reason", "Fehlverhalten");
        proxyNamespace.setDefault("proxy.mute.default_reason", "1");
        proxyNamespace.setDefault("proxy.command.report.default_reason", "Fehlverhalten");
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
        I18N.setDefaultByLang("de_DE", "proxy.command.login.already_logged_in", "§cDu bist bereits eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.login.logged_in", "§aDu bist nun eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.plugins.no_permission", "§aHmm§f, §aich§f, §aglaube§f, §adass§f, §adu§f, §ahier§f, §anichts§f, §afinden§f, §awirst.");
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.not_logged_in", "§cDu bist nicht eingeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.logged_out", "§aDu bist nun ausgeloggt");
        I18N.setDefaultByLang("de_DE", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("de_DE", "proxy.command.teamchat.prefix", "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.too_few_args", "§cBitte benutze §7/ban <spieler> [grund]§c!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_player", "§cDu kannst diesen Spieler nicht bannen!");
        I18N.setDefaultByLang("de_DE", "proxy.command.ban.cant_ban_yourself", "§cDu kannst dich nicht selber bannen!");
        I18N.setDefaultByLang("de_DE", "proxy.player_404", "§cDer Spieler wurde nicht gefunden");
        I18N.setDefaultByLang("de_DE", "proxy.command.kick.too_few_args", "§cBitte benutze §7/kick <spieler> [grund]§c!");
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
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.temporary_banscreen_text", "§eGalaxyCore.net\n\n" +
                "§cDu wurdest bis §e§l{until} §r§cvom Netzwerk gebannt.\n" +
                "§cvon§8: §e{staff}\n\n" +
                "§cVerbleibende Zeit§8: §e{remaining}\n\n" +
                "§cEntbannungsantrag: §e{banscreen_url}");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.permanent_banscreen_text", "§eGalaxyCore.net\n\n" +
                "§cDu wurdest §e§lpermanent §r§cvom Netzwerk gebannt.\n" +
                "§cGrund§8: §e§l{reason}§r\n" +
                "§cvon§8: §e{staff}\n\n" +
                "§cEntbannungsantrag: §e{banscreen_url}");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.temporary_mutescreen_text", "§eGalaxyCore.net\n\n" +
                "{prefix}§cDu wurdest bis §e{until} §caus dem Chat gemutet.\n" +
                "§8» §cGrund§8: §e§l{reason}§r\n" +
                "§8» §cvon§8: §e§l{staff}§r\n\n" +
                "§8» §cVerbleibende Zeit§8: §e{remaining}\n\n" +
                "§cEntbannungsantrag: §e{mutescreen_url}");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.permanent_mutescreen_text", "§eGalaxyCore.net\n\n" +
                "{prefix}§cDu wurdest §epermanent §caus dem Chat gemutet.\n" +
                "§8» §cGrund§8: §e§l{reason}§r\n" +
                "§8» von§8: §e§l{staff}§r\n\n" +
                "§cEntbannungsantrag: §e{mutescreen_url}");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.kick_text", "§eGalaxyCore.net\n\n" +
                "§cDu wurdest vom Netzwerk gekickt.\n" +
                "§cGrund§8: §e§l{reason}§r\n" +
                "§cvon§8: §e§l{staff}");
        I18N.setDefaultByLang("de_DE", "proxy.command.login.logged_in", "§aDu bist nun eingeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.plugins.no_permission", "§fProxyPlugins (9): §aHmm§f, §aich§f, §aglaube§f, §adass§f, §adu§f, §ahier§f, §anichts§f, §afinden§f, §awirst.", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.not_logged_in", "§cDu bist nicht eingeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.logout.logged_out", "§aDu bist nun ausgeloggt", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.adminchat.prefix", "§4AdminChat §8| §r%rank_prefix%%player%§7:%chat_important% ", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.teamchat.prefix", "§7TeamChat §8| §r%rank_prefix%%player%§7:%chat_important% ", true);
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
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.title", "§2Freundesystem §7| §2Hilfe", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.list", "§2 /friend list §8» §aListet alle deine Freunde auf", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.msg", "§2 /friend msg [Spieler] [Nachricht] §8» §aSende einem Freund eine Nachricht", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.add", "§2 /friend add [Spieler] §8» §aStellt eine Freundschaftsanfrage", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.accept", "§2 /friend accept §8» §aAkzeptiere eine Freundschaftsanfrage", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.deny", "§2 /friend deny §8» §aLehne eine Freundschaftsanfrage ab", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.help.remove", "§2 /friend remove [Spieler] §8» §aEntferne einen Freund von deiner Freundesliste", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.list", "§2Deine Freunde: {friends}", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.list.none", "§cKeine");
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.msg.help", "§cBitte nutze §7/friend msg [Spieler] [Nachricht]", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.msg.target", "§2{player} §a-> §2Du§2: §7{msg}", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.msg.source", "§2Du §a-> §2{target}§2: §7{msg}", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.notfound", "§cDieser Spieler wurde nicht gefunden", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.notonline", "§cDieser Spieler ist nicht online", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.friendnotfound", "§cDieser Freund wurde nicht gefunden", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.accept.notexists", "§cDiese Freundschaftsanfrage existiert nicht", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.deny.notexists", "§cDiese Freundschaftsanfrage existiert nicht", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.deny.ok", "§cDiese Freundschaftsanfrage wurde gelöscht", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.accept.ok", "§aDiese Freundschaftsanfrage wurde akzeptiert", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.accept.ok.other", "§a{player} hat deine Freundschaftsanfrage akzeptiert", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.add.help", "§cBitte nutze §7/friend add [Spieler]", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.add.ok", "§aDu hast eine Freundschaftsanfrage erstellt!", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.add.request", "§aDu hast eine Freundschaftsanfrage von {player} erhalten:", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.add.yes", "§aAkzeptieren");
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.add.no", "§cLöschen");
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.remove.help", "§cBitte nutze §7/friend remove [Spieler]", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.remove.ok", "§aDu bist nun nicht mehr mit {player} befreundet", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.friend.remove.other", "§aDu bist nun nicht mehr mit {player} befreundet", true);
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
        I18N.setDefaultByLang("de_DE", "proxy.command.mute.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_mute%");
        I18N.setDefaultByLang("de_DE", "proxy.bansystem.anti_vpn", "§cBitte schalte deine VPN/deinen Proxy aus, um auf diesem Server zu spielen");
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
        I18N.setDefaultByLang("de_DE", "proxy.command.ip.usage", "§cBenutzung: §f/ip <player>", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.ip.ip_of_player", "§cIp von Spieler {player}: {ip}", true);
        I18N.setDefaultByLang("de_DE", "proxy.command.unban.player_not_banned", "§cDer Spieler {player} ist nicht gebannt");
        I18N.setDefaultByLang("de_DE", "proxy.remaining.years", "Jahr(e)");
        I18N.setDefaultByLang("de_DE", "proxy.remaining.days", "Tag(e)");
        I18N.setDefaultByLang("de_DE", "proxy.remaining.hours", "Stunde(n)");
        I18N.setDefaultByLang("de_DE", "proxy.remaining.minutes", "Minute(n)");
        I18N.setDefaultByLang("de_DE", "proxy.remaining.seconds", "Sekunde(n)");

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
        I18N.setDefaultByLang("en_GB", "proxy.command.joinme.joinme_exists", "§cThis JoinMe already exists", true);
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
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.title", "§2Friend System §7| §2Help", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.list", "§2 /friend list §8» §aLists your friends", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.msg", "§2 /friend msg [Spieler] [Nachricht] §8» §aSend a message to a friend", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.add", "§2 /friend add [Spieler] §8» §aCreates a Friend Request", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.accept", "§2 /friend accept §8» §aAccept a Friend Request", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.deny", "§2 /friend deny §8» §aDeny a Friend Request", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.help.remove", "§2 /friend remove [Spieler] §8» §aRemove a Friend from your list", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.list", "§2Your Friends: {friends}", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.list.none", "§cNone");
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.msg.help", "§cPlease use §7/friend msg [player] [message]", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.msg.target", "§2{player} §a-> §2You§2: §7{msg}", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.msg.source", "§2You §a-> §2{target}§2: §7{msg}", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.notfound", "§cThis player does not exist", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.notonline", "§cThis player is not online", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.friendnotfound", "§cThis friend was not found", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.accept.notexists", "§cThis Friend Request does not exists", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.deny.notexists", "§cThis Friend Request does not exists", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.deny.ok", "§cThis Friend Request was deleted", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.accept.ok", "§aYour Friend Request was accepted", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.accept.ok.other", "§a{player} accepted your friend request", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.add.help", "§cPlease use §7/friend add [player]", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.add.ok", "§aYou sent the Friend Request!", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.add.request", "§aYou got a Friend Request from {player}:", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.add.yes", "§aAccept");
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.add.no", "§cDeny");
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.remove.help", "§cPlease use §7/friend remove [player]", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.remove.ok", "§aYou are no longer friends with {player}", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.friend.remove.other", "§aYou are no longer friends with {player}", true);
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
        I18N.setDefaultByLang("en_GB", "proxy.command.mute.reason_list", "§c%id% §8» §6%name% §8» §e%req_permission_mute%");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.anti_vpn", "§cPlease turn of your VPN/Proxy to play on this server");
        I18N.setDefaultByLang("en_GB", "proxy.command.ip.usage", "§cUsage: §f/ip <player>", true);
        I18N.setDefaultByLang("en_GB", "proxy.command.ip.ip_of_player", "§cIp of Player {player}: {ip}", true);
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
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.temporary_banscreen_text", "§eGalaxyCore.net\n\n" +
                "§cYou were banned from the network until §e§l{until}§r§c.\n" +
                "§cfrom§8: §e{staff}\n\n" +
                "§cremaining time§8: §e{remaining}\n\n" +
                "§cunban: §e{banscreen_url}");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.permanent_banscreen_text", "§eGalaxyCore.net\n\n" +
                "§cYou were banned §e§lpermanently §r§cfrom the network.\n" +
                "§creason§8: §e§l{reason}§r\n" +
                "§cfrom§8: §e{staff}\n\n" +
                "§cunban: §e{banscreen_url}");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.temporary_mutescreen_text", "§eGalaxyCore.net\n\n" +
                "{prefix}§cYou were muted from the chat until §e{until}§c.\n" +
                "§8» §creason§8: §e§l{reason}§r\n" +
                "§8» §cfrom§8: §e§l{staff}§r\n\n" +
                "§8» §cremaining time§8: §e{remaining}\n\n" +
                "§cunban: §e{mutescreen_url}");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.permanent_mutescreen_text", "§eGalaxyCore.net\n\n" +
                "{prefix}§cYou were muted §epermanently §cfrom the chat.\n" +
                "§8» §creason§8: §e§l{reason}§r\n" +
                "§8» from§8: §e§l{staff}§r\n\n" +
                "§cunban: §e{mutescreen_url}");
        I18N.setDefaultByLang("en_GB", "proxy.bansystem.kickscreen_text", "§eGalaxyCore.net\n\n" +
                "§cYou were kicked from the Network.\n" +
                "§creason§8: §e§l{reason}§r\n" +
                "§cfrom§8: §e§l{staff}");
        I18N.setDefaultByLang("en_GB", "proxy.command.unban.player_not_banned", "§cPlayer {player} ist not banned");
        I18N.setDefaultByLang("en_GB", "proxy.remaining.years", "Year(s)");
        I18N.setDefaultByLang("en_GB", "proxy.remaining.days", "Day(s)");
        I18N.setDefaultByLang("en_GB", "proxy.remaining.hours", "Hour(s)");
        I18N.setDefaultByLang("en_GB", "proxy.remaining.minutes", "Minute(s)");
        I18N.setDefaultByLang("en_GB", "proxy.remaining.seconds", "Second(s)");

        I18N.load();

        // LUCKPERMS API //
        luckPermsAPI = LuckPermsProvider.get();

        // FRIEND MANAGER //
        friendManager = new FriendManager();

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
        friendCommand = new FriendCommand();

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

        // PROXY COMMAND EXECUTOR //
        MinecraftChannelIdentifier.create("galaxycore", "gmc");

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
