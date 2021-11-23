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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
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
    public static @Nullable PlayerLoader buildLoader(int id) {
        GalaxyCoreProxy proxy = ProxyProvider.getProxy();
        PreparedStatement load = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_playercache WHERE id=?");
        load.setInt(1, id);
        ResultSet loadResult = load.executeQuery();

        if(!loadResult.next()) {
            loadResult.close();
            load.close();
            return null;
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
                loadResult.getLong("coins")
        );

        loadResult.close();
        load.close();

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

        PlayerLoader playerLoader = new PlayerLoader(
                loadResult.getInt("id"),
                UUID.fromString(loadResult.getString("uuid")),
                player.getUsername(),
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
                loadResult.getLong("coins")
        );

        loadResult.close();
        load.close();

        PreparedStatement update = connection.prepareStatement("UPDATE core_playercache SET lastname=?, lastlogin=CURRENT_TIMESTAMP WHERE id=?");
        update.setString(1, playerLoader.getLastName());
        update.setInt(2, playerLoader.getId());

        update.executeUpdate();
        update.close();

        loaderHashMap.put(player.getUniqueId(), playerLoader);

        return playerLoader;

    }

    @SneakyThrows
    public static Date parse(ResultSet resultSet, String field) {
        if(ProxyProvider.getProxy().getDatabaseConfiguration().getInternalConfiguration().getConnection().equals("sqlite")) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString(field));
        }else {
            return resultSet.getDate(field);
        }
    }

    public static Optional<PlayerLoader> load(String arg) throws SQLException {
        GalaxyCoreProxy proxy = ProxyProvider.getProxy();
        Connection connection = proxy.getDatabaseConfiguration().getConnection();
        Optional<PlayerLoader> optionalPlayerLoader = Optional.empty();

        PreparedStatement getID = connection.prepareStatement("SELECT id FROM core_playercache WHERE lastname=?");
        getID.setString(1, arg);

        ResultSet resultSet = getID.executeQuery();

        if(resultSet.next()) {
            optionalPlayerLoader = Optional.ofNullable(buildLoader(resultSet.getInt("id")));
        }

        resultSet.close();
        getID.close();

        return optionalPlayerLoader;
    }
}
