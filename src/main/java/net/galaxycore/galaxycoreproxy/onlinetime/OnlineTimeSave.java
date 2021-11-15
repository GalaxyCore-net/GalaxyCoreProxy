package net.galaxycore.galaxycoreproxy.onlinetime;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

@Getter
public class OnlineTimeSave implements Runnable {
    private final OnlineTime onlineTime;

    public OnlineTimeSave(OnlineTime onlineTime) {
        this.onlineTime = onlineTime;
    }

    @Override
    public void run() {
        ProxyProvider.getProxy().getServer().getAllPlayers().forEach(this::save);
    }

    public void save(Player player) {
        onlineTime.getInterpolator().interpolate(player);
    }
}
