package net.galaxycore.galaxycoreproxy.onlinetime;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.sql.PreparedStatement;

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

    @SneakyThrows
    public void save(Player player) {
        if(PlayerLoader.load(player) == null) return;
        long timeForDB = onlineTime.getInterpolator().interpolate(player);

        PreparedStatement statement = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("UPDATE `core_onlinetime` SET onlinetime=? WHERE id=?");
        statement.setLong(1, timeForDB);
        statement.setInt(2, PlayerLoader.load(player).getId());
        statement.executeUpdate();
        statement.close();
    }
}
