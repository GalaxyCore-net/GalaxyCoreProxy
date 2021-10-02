package net.galaxycore.galaxycoreproxy.bansystem;

import lombok.Getter;
import net.galaxycore.galaxycoreproxy.bansystem.command.BanCommand;
import net.galaxycore.galaxycoreproxy.bansystem.command.UnbanCommand;
import net.galaxycore.galaxycoreproxy.bansystem.listener.PlayerJoinListener;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;

public class BanSystem {

    @Getter
    private final BanManager banManager;

    @Getter
    private final BanCommand banCommand;

    @Getter
    private final UnbanCommand unbanCommand;

    @Getter
    private final PlayerJoinListener playerJoinListener;

    public BanSystem() {
        banManager = new BanManager();

        banCommand = new BanCommand();

        unbanCommand = new UnbanCommand();

        playerJoinListener = new PlayerJoinListener();

        PunishmentReason.loadReasons();
        registerDefaultPunishments();

    }

    private void registerDefaultPunishments() {
        PunishmentReason.registerDefaultReason("VPN",                           "ban.srmod", 0, 100, 0,       true);
        PunishmentReason.registerDefaultReason("Hausverbot",                    "ban.admin", 0, 100, 0,       true);
        PunishmentReason.registerDefaultReason("Bannumbehung",                  "ban.mod",   5, 100, 7257600, false);
        PunishmentReason.registerDefaultReason("Servermanipulation",            "ban.mod",   5, 100, 7257600, false);
        PunishmentReason.registerDefaultReason("Unangebrachtes Bauwerk",        "ban.sup",   1, 100, 345600,  false);
        PunishmentReason.registerDefaultReason("Bots",                          "ban.srsup", 5, 100, 4838400, false);
        PunishmentReason.registerDefaultReason("Rangverkauf",                   "ban.srsup", 4, 100, 4838400, false);
        PunishmentReason.registerDefaultReason("Accountverkauf",                "ban.srsup", 4, 100, 4838400, false);
        PunishmentReason.registerDefaultReason("Namensgebung",                  "ban.sup",   1, 100, 604800,  false);
        PunishmentReason.registerDefaultReason("Unerlaubte Clientmodifikation", "ban.sup",   2, 100, 1209600, false);
        PunishmentReason.registerDefaultReason("Bugusing",                      "ban.sup",   2, 100, 1209600, false);
        PunishmentReason.registerDefaultReason("Rangausnutzung",                "ban.sup",   1, 100, 302400,  false);
        PunishmentReason.registerDefaultReason("Scamming",                      "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Reportausnutzung",              "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Teaming",                       "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Combatlog",                     "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Rechtsextremes Verhalten",      "ban.srsup", 1, 100, 604800,  false);
        PunishmentReason.registerDefaultReason("Datenschutz",                   "ban.srmod", 5, 100, 0,       true);
        PunishmentReason.registerDefaultReason("Werbung",                       "ban.sup",   1, 100, 604800,  false);
        PunishmentReason.registerDefaultReason("Drohung",                       "ban.sup",   1, 100, 7200,    false);
        PunishmentReason.registerDefaultReason("Beleidigung",                   "ban.sup",   1, 100, 7200,    false);
        PunishmentReason.registerDefaultReason("Provokation",                   "ban.sup",   1, 100, 7200,    false);
        PunishmentReason.registerDefaultReason("Respektlosigkeit",              "ban.sup",   2, 100, 14400,   false);
        PunishmentReason.registerDefaultReason("Links schicken",                "ban.sup",   1, 100, 3600,    false);
        PunishmentReason.registerDefaultReason("Sexuelle Anspielungen",         "ban.sup",   3, 100, 28800,   false);
        PunishmentReason.registerDefaultReason("Spam",                          "ban.sup",   1, 100, 3600,    false);
        PunishmentReason.registerDefaultReason("Caps",                          "ban.sup",   1, 100, 3600,    false);
    }

}
