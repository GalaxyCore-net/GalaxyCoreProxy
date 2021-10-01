package net.galaxycore.galaxycoreproxy.bansystem;

import lombok.Getter;
import net.galaxycore.galaxycoreproxy.bansystem.command.BanCommand;
import net.galaxycore.galaxycoreproxy.bansystem.listener.PlayerJoinListener;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;

public class BanSystem {

    @Getter
    private final BanManager banManager;

    @Getter
    private final BanCommand banCommand;

    @Getter
    private final PlayerJoinListener playerJoinListener;

    public BanSystem() {
        banManager = new BanManager();

        banCommand = new BanCommand();

        playerJoinListener = new PlayerJoinListener();

        PunishmentReason.loadReasons();

    }

}
