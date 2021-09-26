package net.galaxycore.galaxycoreproxy.bansystem;

import lombok.Getter;
import net.galaxycore.galaxycoreproxy.bansystem.command.BanCommand;

public class BanSystem {

    @Getter
    private final BanManager banManager;

    @Getter
    private final BanCommand banCommand;

    public BanSystem() {
        banManager = new BanManager();

        banCommand = new BanCommand();
    }

}
