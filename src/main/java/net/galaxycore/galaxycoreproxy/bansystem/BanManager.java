package net.galaxycore.galaxycoreproxy.bansystem;

import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycorecore.utils.DiscordWebhook;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18NPlayerLoader;
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
                if (player.hasPermission("group.team") && !staff.hasPermission("ban.admin") && !player.hasPermission("ban.admin")) {
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
                System.out.println("Incoming ban not found");
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
            player.disconnect(buildBanScreen(player));
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
            System.out.println("Player not found");
            MessageUtils.sendI18NMessage(staff, "proxy.player_404");
            return false;
        }

        Player player = optionalPlayer.get();

        if (!MathUtils.isInt(reason)) {
            MessageUtils.sendI18NMessage(player, "proxy.command.ban.not_a_number");
            return false;
        }

        int banReason = Integer.parseInt(reason);

        return banPlayer(player, banReason, staff);

    }

    public boolean banPlayer(String name, Player staff) {
        return banPlayer(name, ProxyProvider.getProxy()
                .getProxyNamespace().get("proxy.ban.default_reason"), staff);
    }

    public boolean unbanPlayer(String player, String staff) {

        try {

            if (!isPlayerBanned(player)) {
                ProxyProvider.getProxy().getLogger().info("Player " + player + " is not banned");
                return false;
            }

            ProxyProvider.getProxy().getLogger().info(String.valueOf(getPlayerID(player)));
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "DELETE FROM core_bans WHERE userid=?"
            );
            int playerid = getPlayerID(player);
            ps.setInt(1, playerid);
            ps.executeUpdate();
            ps.close();
            createUnbanLogEntry(player, staff);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean kickPlayer(Player player, String reason, Player staff) {

        player.disconnect(buildKickScreen(player, staff, reason));
        createKickLogEntry(player.getUsername(), reason, staff.getUsername());
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
        return kickPlayer(player, ProxyProvider.getProxy().getProxyNamespace().get("proxy.commnad.kick.default_reason"), staff);
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

                createMuteLogEntry(player.getUsername(), String.valueOf(reason), mutePoints, from, until, permanent, staff != null ? staff.getUsername() : "Console");
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

                createMuteLogEntry(player.getUsername(), String.valueOf(reason), mutePoints, from, until, permanent, staff != null ? staff.getUsername() : "Console");
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

    private void createBanLogEntry(String action, String player, String reason, int banPoints, Date from, Date until, boolean permanent, String staff) {
        try {

            int playerid = getPlayerID(player);

            int staffid = getPlayerID(staff);

            //SQL
            @SuppressWarnings("SqlResolve")
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "INSERT INTO core_banlog (`action`, userid, reason, `from`, `until`, permanent, staff, `date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, action);
            ps.setInt(2, playerid);
            ps.setString(3, reason);
            ps.setTimestamp(4, convertUtilDate(from));
            ps.setTimestamp(5, convertUtilDate(until));
            ps.setBoolean(6, permanent);
            ps.setInt(7, staffid);
            ps.setTimestamp(8, new Timestamp(new Date().getTime()));
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
                default:
                    embed.setColor(Color.BLACK);
                    break;
            }

            discordWebhook.addEmbed(embed);
            discordWebhook.execute();

            ProxyProvider.getProxy().getServer().getAllPlayers().stream().filter(player1 -> player1.hasPermission("proxy.bansystem.banlog")).forEach(player1 ->
                    player1.sendMessage(Component.text(MessageUtils.getI18NMessage(player1, "proxy.ban.banlog_entry")
                    .replaceAll("\\{action}", StringUtils.firstLetterUppercase(action))
                    .replaceAll("\\{player}", player)
                    .replaceAll("\\{reason}", reason != null ? reason : "Kein Grund angegeben")
                    .replaceAll("\\{banPoints}", Integer.toString(banPoints))
                    .replaceAll("\\{from}", dtf.format(from))
                    .replaceAll("\\{until}", dtf.format(until))
                    .replaceAll("\\{permanent}", permanent ? "Ja" : "Nein")
                    .replaceAll("\\{staff}", staff))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUnbanLogEntry(String player, String staff) {
        createBanLogEntry("unban", player, null, 0, null, null, false, staff);
    }

    private void createKickLogEntry(String player, String reason, String staff) {
        createBanLogEntry("kick", player, reason, 0, null, null, false, staff);
    }

    private void createMuteLogEntry(String player, String reason, int mutePoints, Date from, Date until, boolean permanent, String staff) {
        createBanLogEntry("mute", player, reason, mutePoints, from, until, permanent, staff);
    }

    private String quote(Object s) {
        return "`" + s.toString() + "`";
    }

    public String replaceBanRelevant(String s, int userID, String customReasonString) {
        try {

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_bans WHERE userid=?"
            );
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();

            PreparedStatement psUserName = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_playercache WHERE id=?"
            );
            psUserName.setInt(1, userID);
            ResultSet rsUserName = psUserName.executeQuery();
            String userName = rsUserName.getString("lastname");
            psUserName.close();
            rsUserName.close();

            PreparedStatement psStaffName = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_playercache WHERE id=?"
            );
            psStaffName.setInt(1, rs.getInt("staff"));
            ResultSet rsStaffName = psStaffName.executeQuery();
            String staffName = rsStaffName.getString("lastname");
            psStaffName.close();
            rsStaffName.close();

            PreparedStatement psReason = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_punishment_reasons WHERE id=?"
            );
            psReason.setInt(1, rs.getInt("reason"));
            ResultSet rsReason = psReason.executeQuery();
            psReason.close();
            rsReason.close();
            PunishmentReason reason = customReasonString == null ? PunishmentReason.loadReason(rs.getInt("reason")) : null;

            s = s
                    .replaceAll("%id%", rs.getString("id"))
                    .replaceAll("%userid%", rs.getString("userid"))
                    .replaceAll("%reasonid%", rs.getString("reasonid"))
                    .replaceAll("%banpoints%", rs.getString("banpoints"))
                    .replaceAll("%from%", parseDate(rs, "from").toString())
                    .replaceAll("%until%", parseDate(rs, "until").toString())
                    .replaceAll("%permanent%", rs.getBoolean("permanent") ? "Ja" : "Nein")
                    .replaceAll("%staffid%", rs.getString("staff"))
                    .replaceAll("%username%", userName)
                    .replaceAll("%staffname%", staffName)
                    .replaceAll("%reason%", customReasonString != null ? customReasonString : reason.getName());

            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    @SneakyThrows
    public String replaceKickRelevant(String s, Player player, Player staff, String reason) {
        PlayerLoader playerLoader = PlayerLoader.load(player);
        PlayerLoader staffLoader = PlayerLoader.load(staff);

        PreparedStatement playerLanuguage = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "SELECT * FROM I18N_languages WHERE lang=?"
        );
        playerLanuguage.setString(1, I18NPlayerLoader.getLocale(player));
        ResultSet rs = playerLanuguage.executeQuery();
        StringBuilder playerLanguageTimeFormat = new StringBuilder();
        if (rs.next())
            playerLanguageTimeFormat.append(replaceDateFormat(rs.getString("date_fmt"))).append(" ").append(rs.getString("time_fmt"));

        s = s
                .replaceAll("%userid%", String.valueOf(playerLoader.getId()))
                .replaceAll("%date%", new SimpleDateFormat(playerLanguageTimeFormat.toString()).format(new Date()))
                .replaceAll("%staffid%", String.valueOf(staffLoader.getId()))
                .replaceAll("%username%", playerLoader.getLastName())
                .replaceAll("%staffname%", staffLoader.getLastName())
                .replaceAll("%reason%", reason);
        return s;
    }

    public Component buildBanScreen(Player player) {
        return Component.text(
                BanSystemProvider.getBanSystem().getBanManager().replaceBanRelevant(
                        MessageUtils.getI18NMessage(player, "proxy.bansystem.banscreen_text"),
                        PlayerLoader.load(player).getId(),
                        null
                )).clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.OPEN_URL,
                ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url")
        ));
    }

    public Component buildKickScreen(Player player, Player staff, String reason) {
        return Component.text(
                replaceKickRelevant(
                        MessageUtils.getI18NMessage(player, "proxy.bansystem.kickscreen_text"),
                        player,
                        staff,
                        reason
                ));
    }

    public String replaceDateFormat(String s) {
        return s.toLowerCase().replaceAll("m", "M");
    }

}
