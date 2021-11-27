package net.galaxycore.galaxycoreproxy.configuration;

import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused") // API Usage
@AllArgsConstructor
@Getter
public class PlayerLoader {

    @Getter
    private static final HashMap<UUID, PlayerLoader> loaderHashMap = new HashMap<>();

    private final int id;
    private final UUID uuid;
    private final String lastName;
    private final Date firstlogin;
    private final Date lastLogin;
    private final Date lastDailyReward;
    private final int banPoints;
    private final int mutePoints;
    private final int warnPoints;
    private final int reports;
    private final boolean teamLogin;
    private final boolean debug;
    private final boolean socialSpy;
    private final boolean commandSpy;
    private final boolean vanished;
    private final boolean nicked;
    private final int lastNick;
    private final long coins;
    private final boolean banned;
    private final boolean muted;
    private final int bans;
    private final int mutes;
    private final int warns;

    public static PlayerLoader loadNew(Player player) {
        PlayerLoader playerLoader = PlayerLoader.buildLoader(player);

        loaderHashMap.put(player.getUniqueId(), playerLoader);

        return playerLoader;
    }

    public static PlayerLoader load(Player player) {
        if(loaderHashMap.containsKey(player.getUniqueId())) {
            return loaderHashMap.get(player.getUniqueId());
        }

        return loadNew(player);
    }

    @SneakyThrows
    private static @Nullable PlayerLoader buildLoader(int id) {
        GalaxyCoreProxy proxy = ProxyProvider.getProxy();
        PreparedStatement load = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_playercache WHERE id=?");
        load.setInt(1, id);
        ResultSet loadResult = load.executeQuery();

        if(!loadResult.next()) {
            loadResult.close();
            load.close();
            return null;
        }

        PreparedStatement banLoad = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_bans WHERE userid=?");
        banLoad.setInt(1, id);
        ResultSet banLoadResult = banLoad.executeQuery();

        PreparedStatement muteLoad = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_mutes WHERE userid=?");
        muteLoad.setInt(1, id);
        ResultSet muteLoadResult = muteLoad.executeQuery();

        PreparedStatement onlineTimeLoad = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT onlinetime FROM core_onlinetime WHERE id=?");
        onlineTimeLoad.setInt(1, id);
        ResultSet onlineTimeResult = onlineTimeLoad.executeQuery();

        PreparedStatement banlogLoad = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_banlog WHERE userid=? AND action='ban'");
        banlogLoad.setInt(1, id);
        ResultSet banlogBanResult = banlogLoad.executeQuery();

        PreparedStatement banlogMuteLoad = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_banlog WHERE userid=? AND action='mute'");
        banlogMuteLoad.setInt(1, id);
        ResultSet banlogMuteResult = banlogMuteLoad.executeQuery();

        PreparedStatement banlogWarnLoad = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_banlog WHERE userid=? AND action='warn'");
        banlogWarnLoad.setInt(1, id);
        ResultSet banlogWarnResult = banlogWarnLoad.executeQuery();

        int bans = 0;
        int mutes = 0;
        int warns = 0;
        while (banlogBanResult.next()) {
            bans++;
        }
        while (banlogMuteResult.next()) {
            mutes++;
        }
        while (banlogWarnResult.next()) {
            warns++;
        }

        PlayerLoader playerLoader = new PlayerLoader(
                id,
                UUID.fromString(loadResult.getString("uuid")),
                loadResult.getString("lastname"),
                parse(loadResult, "firstlogin"),
                parse(loadResult, "lastlogin"),
                parse(loadResult, "last_daily_reward"),
                loadResult.getInt("banpoints"),
                loadResult.getInt("mutepoints"),
                loadResult.getInt("warnpoints"),
                loadResult.getInt("reports"),
                loadResult.getBoolean("teamlogin"),
                loadResult.getBoolean("debug"),
                loadResult.getBoolean("socialspy"),
                loadResult.getBoolean("commandspy"),
                loadResult.getBoolean("vanished"),
                loadResult.getBoolean("nicked"),
                loadResult.getInt("lastnick"),
                loadResult.getLong("coins"),
                banLoadResult.next(),
                muteLoadResult.next(),
                bans,
                mutes,
                warns
        );

        loadResult.close();
        load.close();

        banLoadResult.close();
        banLoad.close();

        muteLoadResult.close();
        muteLoad.close();

        PreparedStatement update = proxy.getDatabaseConfiguration().getConnection().prepareStatement("UPDATE core_playercache SET lastname=?, lastlogin=CURRENT_TIMESTAMP WHERE id=?");
        update.setString(1, playerLoader.getLastName());
        update.setInt(2, playerLoader.getId());

        update.executeUpdate();
        update.close();

        loaderHashMap.put(playerLoader.getUuid(), playerLoader);

        return playerLoader;
    }

    @SneakyThrows
    private static @Nullable PlayerLoader buildLoader(Player player) {
        GalaxyCoreProxy proxy = ProxyProvider.getProxy();
        Connection connection = proxy.getDatabaseConfiguration().getConnection();

        PreparedStatement load = connection.prepareStatement("SELECT * FROM core_playercache WHERE uuid = ?");
        load.setString(1, player.getUniqueId().toString());
        ResultSet loadResult = load.executeQuery();

        if(!loadResult.next()) {
            loadResult.close();
            load.close();
            return null;
        }

        return buildLoader(loadResult.getInt("id"));

    }

    @SneakyThrows
    public static Date parse(ResultSet resultSet, String field) {
        if(ProxyProvider.getProxy().getDatabaseConfiguration().getInternalConfiguration().getConnection().equals("sqlite")) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString(field));
        }else {
            return resultSet.getDate(field);
        }
    }

}
