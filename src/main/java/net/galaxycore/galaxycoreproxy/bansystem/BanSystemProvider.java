package net.galaxycore.galaxycoreproxy.bansystem;

import lombok.Getter;
import lombok.Setter;
import net.galaxycore.galaxycorecore.utils.IProvider;

public class BanSystemProvider implements IProvider<BanSystem> {

    @Getter
    @Setter
    private static BanSystem banSystem;

    @Override
    public BanSystem get() {
        return banSystem;
    }

    public void set(BanSystem banSystem) {
        BanSystemProvider.banSystem = banSystem;
    }

}
