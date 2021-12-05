package net.galaxycore.galaxycoreproxy.bansystem;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import lombok.SneakyThrows;
import net.galaxycore.galaxycorecore.utils.DiscordWebhook;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.PrefixProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.Field;
import net.galaxycore.galaxycoreproxy.utils.MathUtils;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused", "UnusedReturnValue", "DuplicatedCode"})
public class BanManager {

    public boolean banPlayer(Player player, int reason, int banPoints, Date from, Date until, boolean permanent, Player staff) {
        try {

            if (!isPlayerBanned(player.getUsername())) {
                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "INSERT INTO core_bans (userid, reasonid, banpoints, `from`, `until`, permanent, staff) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                ps.setInt(1, PlayerLoader.load(player).getId());
                ps.setInt(2, reason);
                ps.setInt(3, banPoints);
                ps.setTimestamp(4, convertUtilDate(from));
                ps.setTimestamp(5, convertUtilDate(until));
                ps.setBoolean(6, permanent);
                ps.setInt(7, staff != null ? PlayerLoader.load(staff).getId() : 0);
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_playercache SET banpoints=banpoints+? WHERE id=?"
                );
                update.setInt(1, banPoints);
                update.setInt(2, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createBanLogEntry("ban", player.getUsername(), String.valueOf(reason), banPoints, from, until, permanent, staff != null ? staff.getUsername() : "Console");
                player.disconnect(buildBanScreen(player, permanent));
                return true;
            } else {
                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_bans SET reasonid=?, banpoints=?, `from`=?, `until`=?, permanent=?, staff=? WHERE userid=?"
                );
                ps.setInt(1, reason);
                ps.setInt(2, banPoints);
                ps.setTimestamp(3, convertUtilDate(from));
                ps.setTimestamp(4, convertUtilDate(until));
                ps.setBoolean(5, permanent);
                ps.setInt(6, staff != null ? PlayerLoader.load(staff).getId() : 0);
                ps.setInt(7, PlayerLoader.load(player).getId());
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_playercache SET banpoints=banpoints+? WHERE id=?"
                );
                update.setInt(1, banPoints);
                update.setInt(2, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createBanLogEntry("ban", player.getUsername(), String.valueOf(reason), banPoints, from, until, permanent, staff != null ? staff.getUsername() : "Console");
                player.disconnect(buildBanScreen(player, permanent));
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean banPlayer(Player player, int reason, Player staff) {
        try {

            if (staff != null) {
                if (player.hasPermission("group.team") && !staff.hasPermission("ban.admin")) {
                    MessageUtils.sendI18NMessage(staff, "proxy.command.ban.cant_ban_player");
                    return false;
                }
                if (player.getUniqueId() == staff.getUniqueId()) {
                    MessageUtils.sendI18NMessage(staff, "proxy.command.ban.cant_ban_yourself");
                    return false;
                }
            }


            PreparedStatement psIncomingBan = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_punishment_reasons WHERE id=?"
            );
            psIncomingBan.setInt(1, reason);
            ResultSet rsIncomingBan = psIncomingBan.executeQuery();

            PreparedStatement psExistingBansForSameReason = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_banlog WHERE reason=? AND `action`=?"
            );
            psExistingBansForSameReason.setInt(1, reason);
            psExistingBansForSameReason.setString(2, "ban");
            ResultSet rsExistingBansForSameReason = psExistingBansForSameReason.executeQuery();

            if (!rsIncomingBan.next()) {
                ProxyProvider.getProxy().getLogger().debug("Incoming ban not found");
                return false;
            }
            int basePointsToAdd = rsIncomingBan.getInt("points");
            int bansForSameReason = 0;
            while (rsExistingBansForSameReason.next())
                bansForSameReason++;

            int banPointsSum = PlayerLoader.load(player).getBanPoints() + (basePointsToAdd + (
                    basePointsToAdd * (rsIncomingBan.getInt("points_increase_percent") * bansForSameReason) / 100
            ));

            int banTimeSeconds = rsIncomingBan.getInt("duration");
            int banTimeIncrease = banTimeSeconds + (
                    banTimeSeconds * (rsIncomingBan.getInt("points_increase_percent") / 100)
            );
            Date until = new Date((new Date().getTime()) + (rsIncomingBan.getLong("duration") * 1000L));
            until.setTime(until.getTime() + banTimeIncrease);

            boolean permanent = rsIncomingBan.getBoolean("permanent");

            psIncomingBan.close();
            psExistingBansForSameReason.close();
            rsIncomingBan.close();
            rsExistingBansForSameReason.close();
            return banPlayer(player, reason, banPointsSum, new Date(), until, permanent, staff);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean banPlayer(Player player, Player staff) {
        return banPlayer(player, Integer.parseInt(ProxyProvider.getProxy()
                .getProxyNamespace().get("proxy.ban.default_reason")), staff);
    }

    public boolean banPlayer(String name, String reason, Player staff) {

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if (optionalPlayer.isEmpty()) {
            ProxyProvider.getProxy().getLogger().debug("Player not found");
            if (staff != null)
                MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            else
                ProxyProvider.getProxy().getServer().getConsoleCommandSource().sendMessage(Component.text("Player not found"));
            return false;
        }

        Player player = optionalPlayer.get();

        if (!MathUtils.isInt(reason)) {
            MessageUtils.sendI18NMessage(staff, "proxy.command.ban.not_a_number");
            return false;
        }

        int banReason = Integer.parseInt(reason);

        return banPlayer(player, banReason, staff);

    }

    public boolean banPlayer(String name, Player staff) {
        return banPlayer(name, ProxyProvider.getProxy()
                .getProxyNamespace().get("proxy.ban.default_reason"), staff);
    }

    public boolean unbanPlayer(String player, CommandSource staff) {

        try {

            if (!isPlayerBanned(player)) {
                ProxyProvider.getProxy().getLogger().info("Player {} is not banned", player);
                staff.sendMessage(Component.text(MessageUtils.getI18NMessage(staff, "proxy.command.unban.player_not_banned").replace("{player}", player)));
                return false;
            }

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "DELETE FROM core_bans WHERE userid=?"
            );
            int playerid = getPlayerID(player);
            ps.setInt(1, playerid);
            ps.executeUpdate();
            ps.close();
            createUnbanLogEntry(player, staff instanceof Player ? ((Player) staff).getUsername() : "Console");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean unmutePlayer(String player, String staff) {

        try {

            if (!isPlayerMuted(player)) {
                ProxyProvider.getProxy().getLogger().info("Player %s is not banned {}", player);
                return false;
            }

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("DELETE FROM core_mutes WHERE userid=?");
            int playerid = getPlayerID(player);
            ps.setInt(1, playerid);
            ps.executeUpdate();
            ps.close();
            createUnmuteLogEntry(player, staff);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean kickPlayer(Player player, String reason, Player staff) {

        player.disconnect(buildKickScreen(player, staff, reason));
        createKickLogEntry(player.getUsername(), reason, staff != null ? staff.getUsername() : "Console");
        return true;

    }

    public boolean kickPlayer(String playerName, String reason, Player staff) {

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        Player player = optionalPlayer.orElse(null);

        if (player == null) {
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        return kickPlayer(player, reason, staff);

    }

    public boolean kickPlayer(Player player, Player staff) {
        return kickPlayer(player, ProxyProvider.getProxy().getProxyNamespace().get("proxy.command.kick.default_reason"), staff);
    }

    public boolean kickPlayer(String playerName, Player staff) {

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        Player player = optionalPlayer.orElse(null);

        if (player == null) {
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        return kickPlayer(player, staff);

    }

    public boolean mutePlayer(Player player, int reason, int mutePoints, Date from, Date until, boolean permanent, Player staff, String message) {

        try {
            if (!isPlayerMuted(player.getUsername())) {


                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("INSERT INTO core_mutes (userid, reasonid, mutepoints, `from`, `until`, permanent, staff, message) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                );
                ps.setInt(1, PlayerLoader.load(player).getId());
                ps.setInt(2, reason);
                ps.setInt(3, mutePoints);
                ps.setTimestamp(4, convertUtilDate(from));
                ps.setTimestamp(5, convertUtilDate(until));
                ps.setBoolean(6, permanent);
                ps.setInt(7, staff != null ? PlayerLoader.load(staff).getId() : 0);
                ps.setString(8, message);
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_playercache SET mutepoints=mutepoints+? WHERE id=?"
                );
                update.setInt(1, mutePoints);
                update.setInt(2, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createMuteLogEntry(player.getUsername(), String.valueOf(reason), mutePoints, from, until, permanent, staff != null ? staff.getUsername() : "Console", message);
                player.sendMessage(buildMuteScreen(player, permanent));
                return true;
            }else {
                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_mutes SET reasonid=?, mutepoints=?, `from`=?, `until`=?, permanent=?, staff=?, message=? WHERE userid=?"
                );
                ps.setInt(1, reason);
                ps.setInt(2, mutePoints);
                ps.setTimestamp(3, convertUtilDate(from));
                ps.setTimestamp(4, convertUtilDate(until));
                ps.setBoolean(5, permanent);
                ps.setInt(6, staff != null ? PlayerLoader.load(staff).getId() : 0);
                ps.setString(7, message);
                ps.setInt(8, PlayerLoader.load(player).getId());
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_playercache SET mutepoints=mutepoints+? WHERE id=?"
                );
                update.setInt(1, mutePoints);
                update.setInt(2, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createMuteLogEntry(player.getUsername(), String.valueOf(reason), mutePoints, from, until, permanent, staff != null ? staff.getUsername() : "Console", message);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean mutePlayer(Player player, int reason, Player staff, String message) {

        try {

            if (staff != null) {
                if (player.hasPermission("group.team") && !staff.hasPermission("ban.admin") && !player.hasPermission("ban.admin")) {
                    MessageUtils.sendI18NMessage(staff, "proxy.command.mute.cant_mute_player");
                    return false;
                }
                if (player.getUniqueId() == staff.getUniqueId()) {
                    MessageUtils.sendI18NMessage(staff, "proxy.command.mute.cant_mute_yourself");
                    return false;
                }
            }

            PreparedStatement psIncomingMute = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_punishment_reasons WHERE id=?"
            );
            psIncomingMute.setInt(1, reason);
            ResultSet rsIncomingMute = psIncomingMute.executeQuery();

            PreparedStatement psExistingMutesForSameReason = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_banlog WHERE reason=? AND `action`=?"
            );
            psExistingMutesForSameReason.setInt(1, reason);
            psExistingMutesForSameReason.setString(2, "mute");
            ResultSet rsExistingMutesForSameReason = psExistingMutesForSameReason.executeQuery();

            if (!rsIncomingMute.next()) {
                ProxyProvider.getProxy().getLogger().debug("Incoming Mute not found");
                return false;
            }
            int basePointsToAdd = rsIncomingMute.getInt("points");
            int mutesForSameReason = 0;
            while (rsExistingMutesForSameReason.next())
                mutesForSameReason++;

            int banPointsSum = PlayerLoader.load(player).getMutePoints() + (basePointsToAdd + (basePointsToAdd *
                    (rsIncomingMute.getInt("points_increase_percent") * mutesForSameReason) / 100));

            int banTimeSeconds = rsIncomingMute.getInt("duration");
            int banTimeIncrease = banTimeSeconds + (banTimeSeconds * (rsIncomingMute.getInt("points_increase_percent") / 100));
            Date until = new Date((new Date().getTime()) + (rsIncomingMute.getLong("duration") * 1000L));
            until.setTime(until.getTime() + banTimeIncrease);

            boolean permanent = rsIncomingMute.getBoolean("permanent");

            psIncomingMute.close();
            psExistingMutesForSameReason.close();
            rsIncomingMute.close();
            rsExistingMutesForSameReason.close();
            return mutePlayer(player, reason, banPointsSum, new Date(), until, permanent, staff, message);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean mutePlayer(Player player, String reason, Player staff, String message) {

        ProxyProvider.getProxy().getLogger().info(reason);
        if (!MathUtils.isInt(reason)) {
            MessageUtils.sendI18NMessage(player, "proxy.command.ban.not_a_number");
            return false;
        }

        int muteReason = Integer.parseInt(reason);

        return mutePlayer(player, muteReason, staff, message);

    }

    public boolean mutePlayer(Player player, Player staff, String message) {
        return mutePlayer(player, ProxyProvider.getProxy().getProxyNamespace().get("proxy.mute.default_reason"), staff, message);
    }

    public boolean mutePlayer(String playerName, String reason, Player staff, String message) {

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        if (optionalPlayer.isEmpty()) {
            ProxyProvider.getProxy().getLogger().info("Player not found");
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        Player player = optionalPlayer.get();

        return mutePlayer(player, reason, staff, message);

    }

    public boolean mutePlayer(String playerName, Player staff, String message) {
        return mutePlayer(playerName, ProxyProvider.getProxy().getProxyNamespace().get("proxy.mute.default_reason"), staff, message);
    }

    public boolean reportPlayer(String playerName, Player reporter) {
        return reportPlayer(playerName, ProxyProvider.getProxy().getProxyNamespace().get("proxy.command.report.default_reason"), reporter);
    }

    public boolean reportPlayer(String playerName, String reason, Player reporter) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        if (optionalPlayer.isEmpty()) {
            if (reporter == null)
                ProxyProvider.getProxy().getServer().getConsoleCommandSource().sendMessage(Component.text("§cThis Player was not found"));
            else
                MessageUtils.sendI18NMessage(reporter, "proxy.player_404");
            return false;
        }

        Player player = optionalPlayer.get();
        return reportPlayer(player, reason, reporter);
    }

    public boolean reportPlayer(Player player, String reason, Player reporter) {

        try {
            if (reporter != null) {
                if (player.hasPermission("group.team") && !reporter.hasPermission("ban.admin") && !player.hasPermission("ban.admin")) {
                    MessageUtils.sendI18NMessage(reporter, "proxy.command.report.cant_report_player");
                    return false;
                }
                if (player.getUniqueId() == reporter.getUniqueId()) {
                    MessageUtils.sendI18NMessage(reporter, "proxy.command.report.cant_report_yourself");
                    return false;
                }
            }

            if (!isPlayerForReasonReported(player.getUsername(), reason)) {

                PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("INSERT INTO core_reports (reported, reporter, reason, `date`, resolved, staff) " +
                        "VALUES (?, ?, ?, ?, ?, ?)");
                ps.setInt(1, PlayerLoader.load(player).getId());
                ps.setInt(2, reporter != null ? PlayerLoader.load(reporter).getId() : 0);
                ps.setString(3, reason.toLowerCase());
                ps.setTimestamp(4, convertUtilDate(new Date()));
                ps.setBoolean(5, false);
                ps.setInt(6, 0);
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("UPDATE core_playercache SET reports=reports+1 WHERE id=?");
                update.setInt(1, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createReportLogEntry(player.getUsername(), reason, reporter != null ? reporter.getUsername() : "Console");
                return true;

            } else {
                if (reporter != null) {
                    reporter.sendMessage(Component.text(MessageUtils.getI18NMessage(reporter, "proxy.command.report.player_already_reported")
                            .replace("{player}", player.getUsername())));
                } else {
                    ProxyProvider.getProxy().getServer().getConsoleCommandSource().sendMessage(Component.text(MessageUtils.getI18NMessage(ProxyProvider.getProxy().getServer().getConsoleCommandSource(), "proxy.command.report.player_already_reported")
                            .replace("{player}", player.getUsername())));
                }
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean claimReport(String playerName, Player staff) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        if (optionalPlayer.isEmpty()) {
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        Player player = optionalPlayer.get();
        return claimReport(player, staff);
    }

    @SneakyThrows
    public boolean claimReport(Player player, Player staff) {

        if (isPlayerReported(player.getUsername())) {

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "UPDATE core_reports SET staff=? WHERE reported=?"
            );
            ps.setInt(1, staff != null ? PlayerLoader.load(staff).getId() : 0);
            ps.setInt(2, PlayerLoader.load(player).getId());
            ps.executeUpdate();

            PreparedStatement vanish = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "UPDATE core_playercache SET vanished=true WHERE id=?"
            );
            vanish.setInt(1, PlayerLoader.load(player).getId());

            IPlayerManager playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
            if (staff != null) {
                playerManager.getPlayerExecutor(staff.getUniqueId()).connect(player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : ProxyProvider.getProxy().getProxyNamespace().get("proxy.command.report.default_server"));
                MessageUtils.sendI18NMessage(staff, "proxy.command.report.report_claimed");
            }
            return true;

        } else {

            staff.sendMessage(Component.text(MessageUtils.getI18NMessage(staff, "proxy.command.report.player_not_reported")
                    .replace("{player}", player.getUsername())));
            return false;

        }

    }

    public boolean denyReport(String playerName, Player staff) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        if (optionalPlayer.isEmpty()) {
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        Player player = optionalPlayer.get();
        return denyReport(player, staff);
    }

    @SneakyThrows
    public boolean denyReport(Player player, Player staff) {

        if (isPlayerReported(player.getUsername())) {

            PreparedStatement reporterStmt = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_reports WHERE reported=?"
            );
            reporterStmt.setInt(1, PlayerLoader.load(player).getId());
            ResultSet rs = reporterStmt.executeQuery();
            if (rs.next()) {
                int reporterID = rs.getInt("reporter");
                if (reporterID > 0) {
                    PreparedStatement playerCache = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                            "SELECT * FROM core_playercache WHERE id=?"
                    );
                    playerCache.setInt(1, reporterID);
                    ResultSet playerCacheRs = playerCache.executeQuery();
                    if (!playerCacheRs.next()) {
                        return true;
                    }
                    UUID uuid = UUID.fromString(playerCacheRs.getString("uuid"));
                    Optional<Player> optionalReportPlayer = ProxyProvider.getProxy().getServer().getPlayer(uuid);
                    optionalReportPlayer.ifPresent(reportPlayer -> MessageUtils.sendI18NMessage(reportPlayer, "proxy.command.report.report_denied"));
                }
                rs.close();
                reporterStmt.close();
            }

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "DELETE FROM core_reports WHERE reported=?"
            );
            ps.setInt(1, PlayerLoader.load(player).getId());
            ps.executeUpdate();
            MessageUtils.sendI18NMessage(staff, "proxy.command.report.this_report_was_denied");
            return true;
        } else {

            staff.sendMessage(Component.text(MessageUtils.getI18NMessage(staff, "proxy.command.report.player_not_reported")
                    .replace("{player}", player.getUsername())));
            return false;

        }

    }

    public boolean closeReport(String playerName, Player staff) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(playerName);

        if (optionalPlayer.isEmpty()) {
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        Player player = optionalPlayer.get();
        return closeReport(player, staff);
    }

    @SneakyThrows
    public boolean closeReport(Player player, Player staff) {
        PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "SELECT * FROM core_reports WHERE reported=?"
        );
        ps.setInt(1, PlayerLoader.load(player).getId());
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            staff.sendMessage(Component.text(MessageUtils.getI18NMessage(staff, "proxy.command.report.player_not_reported")
                    .replace("{player}", player.getUsername())));
            return false;
        }
        if ((PlayerLoader.load(staff).getId() == rs.getInt("staff")) || staff.hasPermission("proxy.command.report.close.bypass")) {
            rs.close();
            ps.close();
            PreparedStatement delete = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "DELETE FROM core_reports WHERE reported=?"
            );
            delete.setInt(1, PlayerLoader.load(player).getId());
            delete.executeUpdate();
            delete.close();
            MessageUtils.sendI18NMessage(staff, "proxy.command.report.report_closed");
            return true;
        } else {
            MessageUtils.sendI18NMessage(staff, "proxy.command.report.only_claimed_staff");
            return false;
        }
    }

    @SneakyThrows
    public int getPlayerID(String playerName) {
        PreparedStatement stmt = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "SELECT * FROM core_playercache WHERE lastname=?"
        );
        stmt.setString(1, playerName);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getInt("id");
        return 0;
    }

    public boolean isPlayerBanned(String player) {

        try {

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_bans WHERE userid = ?"
            );
            ps.setInt(1, getPlayerID(player));

            ResultSet rs = ps.executeQuery();
            boolean hasNext = rs.next();
            ps.close();
            rs.close();
            return hasNext;

        } catch (Exception ignore) {
            return false;
        }

    }

    public boolean isPlayerMuted(String player) {

        try {
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_mutes WHERE userid=?"
            );
            ps.setInt(1, getPlayerID(player));
            ResultSet rs = ps.executeQuery();
            boolean hasNext = rs.next();
            ps.close();
            rs.close();
            return hasNext;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean isPlayerForReasonReported(String player, String reason) {
        try {
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_reports WHERE reported=? AND reason=?"
            );
            ps.setInt(1, getPlayerID(player));
            ps.setString(2, reason.toLowerCase());
            ResultSet rs = ps.executeQuery();
            boolean hasNext = rs.next();
            rs.close();
            ps.close();
            return hasNext;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPlayerReported(String player) {
        try {
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_reports WHERE reported=?"
            );
            ps.setInt(1, getPlayerID(player));
            ResultSet rs = ps.executeQuery();
            boolean hasNext = rs.next();
            rs.close();
            ps.close();
            return hasNext;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SneakyThrows
    private static Date parseDate(ResultSet resultSet, String field) {
        if (ProxyProvider.getProxy().getDatabaseConfiguration().getInternalConfiguration().getConnection().equals("sqlite"))
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString(field));
        else
            return resultSet.getDate(field);
    }

    private static java.sql.Timestamp convertUtilDate(Date date) {
        return new java.sql.Timestamp(date == null ? new Date().getTime() : date.getTime());
    }

    private void createBanLogEntry(String action, String player, String reason, int banPoints, Date from, Date until, boolean permanent, String staff, Field... optionalFields) {
        try {

            int playerid = getPlayerID(player);

            int staffid = getPlayerID(staff);

            //SQL
            @SuppressWarnings("SqlResolve")
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "INSERT INTO core_banlog (`action`, userid, reason, `until`, permanent, staff, `date`) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, action);
            ps.setInt(2, playerid);
            ps.setString(3, reason);
            ps.setTimestamp(4, convertUtilDate(until));
            ps.setBoolean(5, permanent);
            ps.setInt(6, staffid);
            ps.setTimestamp(7, new Timestamp(new Date().getTime()));
            ps.executeUpdate();
            ps.close();

            // Discord
            SimpleDateFormat dtf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            DiscordWebhook discordWebhook = new DiscordWebhook(ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banlog_webhook"));

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setAuthor("GalaxyCore » BanLog", "", "");
            embed.setTitle(StringUtils.firstLetterUppercase(action));
            embed.setThumbnail("https://minotar.net/bust/" + player + "/190.png");
            embed.setDescription(quote(player));

            if (reason != null)
                embed.addField("Grund: ", MathUtils.isInt(reason) ? PunishmentReason.loadReason(Integer.parseInt(reason)).getName() : reason, false);

            if (banPoints != -1 && !permanent && !action.equals("unban"))
                embed.addField("Banpunkte:", String.valueOf(banPoints), false);

            if (from != null)
                embed.addField("Von:", dtf.format(from), false);

            if (until != null)
                embed.addField("Bis:", permanent ? "Permanent" : dtf.format(until), false);

            embed.addField("Staff:", quote(staff), false);

            switch (action) {
                case "ban":
                    embed.setColor(Color.GREEN);
                    break;
                case "unban":
                    embed.setColor(Color.RED);
                    break;
                case "report":
                    embed.setColor(Color.YELLOW);
                    break;
                default:
                    embed.setColor(Color.BLACK);
                    break;
            }

            for (Field field : optionalFields) {
                embed.addField(StringUtils.firstLetterUppercase(field.getKey().split("\\.")[field.getKey().split("\\.").length - 1]), "`" + field.getValue() + "`", false);
            }

            discordWebhook.addEmbed(embed);
            discordWebhook.execute();

            for (Player player1 : ProxyProvider.getProxy().getServer().getAllPlayers()) {

                if (!player1.hasPermission("proxy.bansystem.banlog")) {
                    continue;
                }

                String i18nKey = MessageUtils.getI18NMessage(player1, "proxy.ban.banlog_entry");
                i18nKey = i18nKey.replace("{action}", StringUtils.firstLetterUppercase(action));
                i18nKey = i18nKey.replace("{player}", player);
                i18nKey = i18nKey.replace("{reason}", reason != null ? reason : "No reason provided");
                i18nKey = i18nKey.replace("{banPoints}", Integer.toString(banPoints));
                i18nKey = i18nKey.replace("{from}", from != null ? dtf.format(from) : "N/A");
                i18nKey = i18nKey.replace("{until}", until != null ? dtf.format(until) : "N/A");
                i18nKey = i18nKey.replace("{permanent}", permanent ? "Ja" : "Nein");
                i18nKey = i18nKey.replace("{staff}", staff);
                StringBuilder msg = new StringBuilder(i18nKey);

                boolean lineBreak = true;
                for (Field field : optionalFields) {
                    msg.append(lineBreak ? "\n" : "").append(MessageUtils.getI18NMessage(player1, field.getKey())).append(": ").append(field.getValue()).append("\n");
                    lineBreak = false;
                }
                player1.sendMessage(Component.text(msg.toString()));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUnbanLogEntry(String player, String staff) {
        createBanLogEntry("unban", player, null, 0, null, null, false, staff);
    }

    private void createUnmuteLogEntry(String player, String staff) {
        createBanLogEntry("unmute", player, null, 0, null, null, false, staff);
    }

    private void createKickLogEntry(String player, String reason, String staff) {
        createBanLogEntry("kick", player, reason, -1, null, null, false, staff);
    }

    private void createMuteLogEntry(String player, String reason, int mutePoints, Date from, Date until, boolean permanent, String staff, String message) {
        createBanLogEntry("mute", player, reason, mutePoints, from, until, permanent, staff, new Field("proxy.bansystem.mute.message", message));
    }

    private void createReportLogEntry(String player, String reason, String staff) {
        createBanLogEntry("report", player, reason, -1, null, null, false, staff);
        ProxyProvider.getProxy().getServer().getAllPlayers().stream().filter(player1 -> player1.hasPermission("proxy.bansystem.new_report")).forEach(player1 -> {

            player1.sendMessage(Component.text(MessageUtils.getI18NMessage(player1, "proxy.command.report.accept"))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/report claim " + player)));
            player1.sendMessage(Component.text(MessageUtils.getI18NMessage(player1, "proxy.command.report.deny"))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/report deny " + player)));
        });
    }

    private String quote(Object s) {
        return "`" + s.toString() + "`";
    }

    public String replaceBanRelevant(String s, Player player, String customReasonString) {
        try {

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_bans WHERE userid=?"
            );
            ps.setInt(1, PlayerLoader.load(player).getId());
            ResultSet rs = ps.executeQuery();
            rs.next();

            PreparedStatement psUserName = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_playercache WHERE id=?"
            );
            psUserName.setInt(1, PlayerLoader.load(player).getId());
            ResultSet rsUserName = psUserName.executeQuery();
            rsUserName.next();
            String userName = rsUserName.getString("lastname");
            psUserName.close();
            rsUserName.close();

            PreparedStatement psStaffName = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_playercache WHERE id=?"
            );
            psStaffName.setInt(1, rs.getInt("staff"));
            ResultSet rsStaffName = psStaffName.executeQuery();
            String staffName = "Console";
            if (rsStaffName.next())
                staffName = rsStaffName.getString("lastname");
            psStaffName.close();
            rsStaffName.close();

            PunishmentReason reason = customReasonString == null ? PunishmentReason.loadReason(rs.getInt("reasonid")) : null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date from = parseDate(rs, "from");
            Date until = parseDate(rs, "until");
            long[] remaining = calculateRemainingTime(until);
            String remainingFormatted = remaining[0] + " " + MessageUtils.getI18NMessage(player, "proxy.remaining.years") + ", " +
                    remaining[1] + " " + MessageUtils.getI18NMessage(player, "proxy.remaining.days") + ", " +
                    remaining[2] + " " + MessageUtils.getI18NMessage(player, "proxy.remaining.hours") + ", " +
                    remaining[3] + " " + MessageUtils.getI18NMessage(player, "proxy.remaining.minutes") + ", " +
                    remaining[4] + " " + MessageUtils.getI18NMessage(player, "proxy.remaining.seconds") + ", ";

            s = s
                    .replace("{id}", rs.getString("id"))
                    .replace("{userid}", rs.getString("userid"))
                    .replace("{reasonid}", rs.getString("reasonid"))
                    .replace("{banpoints}", rs.getString("banpoints"))
                    .replace("{from}", sdf.format(from))
                    .replace("{until}", sdf.format(until))
                    .replace("{permanent}", rs.getBoolean("permanent") ? "Ja" : "Nein")
                    .replace("{staffid}", rs.getString("staff"))
                    .replace("{username}", userName)
                    .replace("{staff}", staffName)
                    .replace("{reason}", customReasonString != null ? customReasonString : reason.getName())
                    .replace("{banscreen_url}", ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url"))
                    .replace("{remaining}", remainingFormatted);

            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    public String replaceKickRelevant(String s, Player staff, String reason) {
        return s
                .replace("{reason}", reason)
                .replace("{staff}", staff != null ? staff.getUsername() : "Console");
    }

    public String replaceMuteRelevant(String s, Player player) {
        try {
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_mutes WHERE userid=?"
            );
            ps.setInt(1, getPlayerID(player.getUsername()));
            ResultSet rs = ps.executeQuery();
            rs.next();

            PreparedStatement psStaffName = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_playercache WHERE id=?"
            );
            psStaffName.setInt(1, rs.getInt("staff"));
            ResultSet rsStaffName = psStaffName.executeQuery();
            String staffName = "Console";
            if (rsStaffName.next()) {
                staffName = rsStaffName.getString("lastname");
            }
            psStaffName.close();
            rsStaffName.close();

            PunishmentReason reason = PunishmentReason.loadReason(rs.getInt("reasonid"));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date from = parseDate(rs, "from");
            Date until = parseDate(rs, "until");
            Date diff = new Date(until.getTime() - new Date().getTime());

            s = s
                    .replace("{id}", rs.getString("id"))
                    .replace("{userid}", rs.getString("userid"))
                    .replace("{reasonid}", rs.getString("reasonid"))
                    .replace("{mutepoints}", rs.getString("mutepoints"))
                    .replace("{from}", sdf.format(from))
                    .replace("{until}", sdf.format(until))
                    .replace("{permanent}", rs.getBoolean("permanent") ? "Ja" : "Nein")
                    .replace("{staffid}", rs.getString("staff"))
                    .replace("{username}", player.getUsername())
                    .replace("{staff}", staffName)
                    .replace("{reason}", reason.getName())
                    .replace("{mutescreen_url}", ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url"))
                    .replace("{remaining}", sdf.format(diff))
                    .replace("{prefix}", PrefixProvider.getPrefix());

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return s;
    }

    public Component buildBanScreen(Player player, boolean permanent) {
        return Component.text(replaceBanRelevant(permanent ?
                                MessageUtils.getI18NMessage(player, "proxy.bansystem.permanent_banscreen_text") :
                                MessageUtils.getI18NMessage(player, "proxy.bansystem.temporary_banscreen_text"),
                        player,
                        null))
                .clickEvent(ClickEvent.clickEvent(
                        ClickEvent.Action.OPEN_URL,
                        ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url")
                ));
    }

    public Component buildKickScreen(Player player, Player staff, String reason) {
        return Component.text(replaceKickRelevant(
                MessageUtils.getI18NMessage(player, "proxy.bansystem.kick_text"),
                staff,
                reason
        ));
    }

    public Component buildVPNScreen(Player player) {
        return Component.text(MessageUtils.getI18NMessage(player, "proxy.bansystem.anti_vpn"));
    }

    public Component buildMuteScreen(Player player, boolean permanent) {
        return Component.text(
                replaceMuteRelevant(
                        permanent ? MessageUtils.getI18NMessage(player, "proxy.bansystem.permanent_mutescreen_text") :
                                MessageUtils.getI18NMessage(player, "proxy.bansystem.temporary_mutescreen_text"),
                        player
                )
        ).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url")));
    }

    public String replaceDateFormat(String s) {
        return s.toLowerCase().replace("m", "M");
    }

    private long[] calculateRemainingTime(Date until) {

        long timeDiff = until.getTime() - new Date().getTime();

        long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
        timeDiff -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.HOURS.convert(timeDiff, TimeUnit.MILLISECONDS);
        timeDiff -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MINUTES.convert(timeDiff, TimeUnit.MILLISECONDS);
        timeDiff -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.SECONDS.convert(timeDiff, TimeUnit.MILLISECONDS);

        long years = 0;
        while (days >= 365) {
            years++;
            days -= 365;
        }

        return new long[]{years, days, hours, minutes, seconds};

    }

}
