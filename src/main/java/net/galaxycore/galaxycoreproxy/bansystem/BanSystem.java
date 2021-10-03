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
        PunishmentReason.registerDefaultReason("VPN",                           "ban.admin", "ban.srmod", 0, 100, 0,       true);
        PunishmentReason.registerDefaultReason("Hausverbot",                    "ban.admin", "ban.admin", 0, 100, 0,       true);
        PunishmentReason.registerDefaultReason("Bannumbehung",                  "ban.admin", "ban.mod",   5, 100, 7257600, false);
        PunishmentReason.registerDefaultReason("Servermanipulation",            "ban.mod",   "ban.mod",   5, 100, 7257600, false);
        PunishmentReason.registerDefaultReason("Unangebrachtes Bauwerk",        "ban.admin", "ban.sup",   1, 100, 345600,  false);
        PunishmentReason.registerDefaultReason("Bots",                          "ban.admin", "ban.srsup", 5, 100, 4838400, false);
        PunishmentReason.registerDefaultReason("Rangverkauf",                   "ban.srsup", "ban.srsup", 4, 100, 4838400, false);
        PunishmentReason.registerDefaultReason("Accountverkauf",                "ban.srsup", "ban.srsup", 4, 100, 4838400, false);
        PunishmentReason.registerDefaultReason("Namensgebung",                  "ban.admin", "ban.sup",   1, 100, 604800,  false);
        PunishmentReason.registerDefaultReason("Unerlaubte Clientmodifikation", "ban.admin", "ban.sup",   2, 100, 1209600, false);
        PunishmentReason.registerDefaultReason("Bugusing",                      "ban.admin", "ban.admin", 2, 100, 1209600, false);
        PunishmentReason.registerDefaultReason("Rangausnutzung",                "ban.admin", "ban.sup",   1, 100, 302400,  false);
        PunishmentReason.registerDefaultReason("Scamming",                      "ban.sup",   "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Reportausnutzung",              "ban.sup",   "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Teaming",                       "ban.admin", "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Combatlog",                     "ban.admin", "ban.sup",   1, 100, 172800,  false);
        PunishmentReason.registerDefaultReason("Rechtsextremes Verhalten",      "ban.srsup", "ban.srsup", 1, 100, 604800,  false);
        PunishmentReason.registerDefaultReason("Datenschutz",                   "ban.srmod", "ban.srmod", 5, 100, 0,       true);
        PunishmentReason.registerDefaultReason("Werbung",                       "ban.sup",   "ban.admin", 1, 100, 604800,  false);
        PunishmentReason.registerDefaultReason("Drohung",                       "ban.sup",   "ban.admin", 1, 100, 7200,    false);
        PunishmentReason.registerDefaultReason("Beleidigung",                   "ban.sup",   "ban.admin", 1, 100, 7200,    false);
        PunishmentReason.registerDefaultReason("Provokation",                   "ban.sup",   "ban.srsup", 1, 100, 7200,    false);
        PunishmentReason.registerDefaultReason("Respektlosigkeit",              "ban.sup",   "ban.sup",   2, 100, 14400,   false);
        PunishmentReason.registerDefaultReason("Links schicken",                "ban.sup",   "ban.admin", 1, 100, 3600,    false);
        PunishmentReason.registerDefaultReason("Sexuelle Anspielungen",         "ban.sup",   "ban.admin", 3, 100, 28800,   false);
        PunishmentReason.registerDefaultReason("Spam",                          "ban.sup",   "ban.mod",   1, 100, 3600,    false);
        PunishmentReason.registerDefaultReason("Caps",                          "ban.sup",   "ban.admin", 1, 100, 3600,    false);
    }

}
