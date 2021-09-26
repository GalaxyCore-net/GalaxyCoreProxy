package net.galaxycore.galaxycoreproxy.bansystem;

import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MathUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class BanManager {

    public boolean banPlayer(Player player, int reason, int banPoints, Date from, Date until, boolean permanent, int staff) {
        try {

            if(!isPlayerBanned(player)) {
                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "INSERT INTO core_bans (userid, reasonid, banpoints, `from`, `until`, permanent, staff) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                ps.setInt(1, PlayerLoader.load(player).getId());
                ps.setInt(2, reason);
                ps.setInt(3, banPoints);
                ps.setDate(4, convertUtilDate(from));
                ps.setDate(5, convertUtilDate(until));
                ps.setBoolean(6, permanent);
                ps.setInt(7, staff);
                ps.executeUpdate();
                createBanLogEntry(player, reason, banPoints, from, until, permanent, staff);
                return true;
            }else {
                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_bans SET reasonid=?, banpoints=?, `from`=?, `until`=?, permanent=?, staff=? WHERE userid=?"
                );
                ps.setInt(1, reason);
                ps.setInt(2, banPoints);
                ps.setDate(3, convertUtilDate(from));
                ps.setDate(4, convertUtilDate(until));
                ps.setBoolean(5, permanent);
                ps.setInt(6, staff);
                ps.setInt(6, PlayerLoader.load(player).getId());
                ps.executeUpdate();
                createBanLogEntry(player, reason, banPoints, from, until, permanent, staff);
                return true;
            }

        }catch (Exception ignore) {
            return false;
        }

    }

    public boolean banPlayer(Player player, int reason) {
        return banPlayer(player, reason, 1, new Date(), null, true, 1);
    }

    public boolean banPlayer(Player player) {
        return banPlayer(player, Integer.parseInt(ProxyProvider.getProxy()
                        .getProxyNamespace().get("proxy.ban.default_reason")));
    }

    public boolean banPlayer(String name, String reason) {
        if(!MathUtils.isInt(reason))
            return false;

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if(optionalPlayer.isEmpty()) {
            return false;
        }

        Player player = optionalPlayer.get();
        int banReason = Integer.parseInt(reason);

        return banPlayer(player, banReason);

    }

    public boolean banPlayer(String name) {
        return banPlayer(name, ProxyProvider.getProxy()
                .getProxyNamespace().get("proxy.ban.default_reason"));
    }

    public boolean unbanPlayer(String name, String staffName) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);
        Optional<Player> optionalStaff = ProxyProvider.getProxy().getServer().getPlayer(staffName);

        if(optionalPlayer.isEmpty() || optionalStaff.isEmpty())
            return false;

        Player player = optionalPlayer.get();
        Player staff = optionalStaff.get();

        return unbanPlayer(player, staff);

    }

    public boolean unbanPlayer(String name, Player staff) {

        Optional<Player> optionalplayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if(optionalplayer.isEmpty())
            return false;

        return unbanPlayer(optionalplayer.get(), staff);

    }

    public boolean unbanPlayer(Player player, String staffName) {

        Optional<Player> optionalStaff = ProxyProvider.getProxy().getServer().getPlayer(staffName);

        if(optionalStaff.isEmpty())
            return false;

        return unbanPlayer(player, optionalStaff.get());

    }

    public boolean unbanPlayer(Player player, Player staff) {

        try {

            if(!isPlayerBanned(player))
                return true;

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "DELETE FROM core_bans WHERE userid=?"
            );
            ps.setInt(1, PlayerLoader.load(player).getId());
            ps.executeUpdate();
            createUnbanLogEntry(player, staff);
            return true;

        }catch (Exception ignore) {
            return false;
        }

    }

    public boolean isPlayerBanned(Player player) {

        try {

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_bans WHERE userid = ?"
            );
            ps.setInt(1, PlayerLoader.load(player).getId());

            ResultSet rs = ps.executeQuery();
            return rs.next();

        }catch (Exception ignore) {
            return false;
        }

    }

    public boolean isPlayerBanned(String name) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if(optionalPlayer.isEmpty())
            return false;

        return isPlayerBanned(optionalPlayer.get());
    }

    @SneakyThrows
    private static Date parseDate(ResultSet resultSet, String field) {
        if(ProxyProvider.getProxy().getDatabaseConfiguration().getInternalConfiguration().getConnection().equals("sqlite"))
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString(field));
        else
            return resultSet.getDate(field);
    }

    private static java.sql.Date convertUtilDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    private void createBanLogEntry(Player player, int reason, int banPoints, Date from, Date until, boolean permanent, int staff) {
        //TODO: Create Ban Log Entry in Database Table, Discord
    }

    private void createUnbanLogEntry(Player player, Player staff) {
        //TODO: Create Ban Log Entry in Database Table, Discord
    }

}
