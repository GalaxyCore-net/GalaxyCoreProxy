package net.galaxycore.galaxycoreproxy.onlinetime;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.DatabaseConfiguration;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@Getter
public class OnlineTimeInterpolator {
    private final HashMap<Player, Long> currentInterpolationOnlineTime = new HashMap<>(); /* Millis */
    private final HashMap<Player, Long> lastSaveMillis = new HashMap<>();
    private final OnlineTime onlineTime;
    private final DatabaseConfiguration databaseConfiguration;

    public OnlineTimeInterpolator(OnlineTime onlineTime) {
        ProxyProvider.getProxy().registerListener(this);
        this.databaseConfiguration = ProxyProvider.getProxy().getDatabaseConfiguration();
        this.onlineTime = onlineTime;
    }

    @Subscribe
    public void quitEvent(DisconnectEvent event) {
        this.getOnlineTime().getSave().save(event.getPlayer());
        getCurrentInterpolationOnlineTime().remove(event.getPlayer());
        getLastSaveMillis().remove(event.getPlayer());
    }

    @SneakyThrows
    public void registerJoin(Player player) {
        if (PlayerLoader.load(player) == null) return;

        PreparedStatement select_query = databaseConfiguration.getConnection().prepareStatement("SELECT `onlinetime` FROM `core_onlinetime` WHERE `id`=?");
        select_query.setInt(1, PlayerLoader.load(player).getId());
        ResultSet select_answer = select_query.executeQuery();

        if(!select_answer.next()) {
            select_answer.close();
            select_query.close();

            PreparedStatement statement = databaseConfiguration.getConnection().prepareStatement("INSERT INTO `core_onlinetime` VALUES (?, 0)");
            statement.setInt(1, PlayerLoader.load(player).getId());
            statement.executeUpdate();

            getCurrentInterpolationOnlineTime().put(player, 0L);
            getLastSaveMillis().put(player, System.currentTimeMillis());
            return;
        }

        getCurrentInterpolationOnlineTime().put(player, select_answer.getLong("onlinetime"));
        getLastSaveMillis().put(player, System.currentTimeMillis());
        select_answer.close();
        select_query.close();
    }

    public long interpolate(Player player) {
        if(PlayerLoader.load(player) == null) return 0;
        long currentInterpolationOnlineTime =  getCurrentInterpolationOnlineTime().get(player) + System.currentTimeMillis() - getLastSaveMillis().get(player);
        getLastSaveMillis().put(player, System.currentTimeMillis());
        getCurrentInterpolationOnlineTime().put(player, currentInterpolationOnlineTime);
        return currentInterpolationOnlineTime;
    }
}
