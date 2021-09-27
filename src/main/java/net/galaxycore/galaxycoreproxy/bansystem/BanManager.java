package net.galaxycore.galaxycoreproxy.bansystem;

import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycorecore.utils.DiscordWebhook;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MathUtils;
import net.galaxycore.galaxycoreproxy.utils.StringUtils;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue", "DuplicatedCode"})
public class BanManager {

    public boolean banPlayer(Player player, int reason, int banPoints, Date from, Date until, boolean permanent, int staff) {
        try {

            if (!isPlayerBanned(player)) {
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
                ps.close();
                createBanLogEntry("ban", player, reason, banPoints, from, until, permanent, staff);
                return true;
            } else {
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
                ps.close();
                createBanLogEntry("ban", player, reason, banPoints, from, until, permanent, staff);
                return true;
            }

        } catch (Exception ignore) {
            return false;
        }

    }

    public boolean banPlayer(Player player, int reason, int staff) {
        try {

            PreparedStatement psBan = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_bans WHERE userid=?"
            );
            psBan.setInt(1, PlayerLoader.load(player).getId());
            ResultSet rsBan = psBan.executeQuery();

            PreparedStatement psIncomingBan = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_punishment_reasons WHERE id=?"
            );
            psIncomingBan.setInt(1, reason);
            ResultSet rsIncomingBan = psIncomingBan.executeQuery();

            PreparedStatement psExistingBansForSameReason = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_banlog WHERE reasonid=?"
            );
            psExistingBansForSameReason.setInt(1, reason);
            ResultSet rsExistingBansForSameReason = psExistingBansForSameReason.executeQuery();
            rsExistingBansForSameReason.last();

            int basePointsToAdd = rsIncomingBan.getInt("points");
            int bansForSameReason = rsExistingBansForSameReason.getRow();

            int banPointsSum = rsBan.getInt("banpoints") + (basePointsToAdd + (
                    basePointsToAdd * (rsIncomingBan.getInt("points_increase_percent") * bansForSameReason) / 100
            ));

            int banTimeSeconds = rsIncomingBan.getInt("duration");
            int banTimeIncrease = banTimeSeconds + (
                    banTimeSeconds * (rsIncomingBan.getInt("points_increase_percent") / 100)
            );
            Date until = parseDate(rsBan, "`until`");
            until.setTime(until.getTime() * 1000 + banTimeIncrease);

            boolean permanent = rsIncomingBan.getBoolean("permanent");

            psBan.close();
            psIncomingBan.close();
            psExistingBansForSameReason.close();
            rsBan.close();
            rsIncomingBan.close();
            rsExistingBansForSameReason.close();
            return banPlayer(player, reason, banPointsSum, new Date(), until, permanent, staff);

        } catch (Exception ignore) {
            return false;
        }
    }

    public boolean banPlayer(Player player, int reason) {
        return banPlayer(player, reason, 0);
    }

    public boolean banPlayer(Player player) {
        return banPlayer(player, Integer.parseInt(ProxyProvider.getProxy()
                .getProxyNamespace().get("proxy.ban.default_reason")));
    }

    public boolean banPlayer(String name, String reason) {
        if (!MathUtils.isInt(reason))
            return false;

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if (optionalPlayer.isEmpty()) {
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

        if (optionalPlayer.isEmpty() || optionalStaff.isEmpty())
            return false;

        Player player = optionalPlayer.get();
        Player staff = optionalStaff.get();

        return unbanPlayer(player, staff);

    }

    public boolean unbanPlayer(String name, Player staff) {

        Optional<Player> optionalplayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if (optionalplayer.isEmpty())
            return false;

        return unbanPlayer(optionalplayer.get(), staff);

    }

    public boolean unbanPlayer(Player player, String staffName) {

        Optional<Player> optionalStaff = ProxyProvider.getProxy().getServer().getPlayer(staffName);

        if (optionalStaff.isEmpty())
            return false;

        return unbanPlayer(player, optionalStaff.get());

    }

    public boolean unbanPlayer(Player player, Player staff) {

        try {

            if (!isPlayerBanned(player))
                return true;

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "DELETE FROM core_bans WHERE userid=?"
            );
            ps.setInt(1, PlayerLoader.load(player).getId());
            ps.executeUpdate();
            ps.close();
            createUnbanLogEntry(player, staff);
            return true;

        } catch (Exception ignore) {
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
            boolean hasNext = rs.next();
            ps.close();
            rs.close();
            return hasNext;

        } catch (Exception ignore) {
            return false;
        }

    }

    public boolean isPlayerBanned(String name) {
        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if (optionalPlayer.isEmpty())
            return false;

        return isPlayerBanned(optionalPlayer.get());
    }

    @SneakyThrows
    private static Date parseDate(ResultSet resultSet, String field) {
        if (ProxyProvider.getProxy().getDatabaseConfiguration().getInternalConfiguration().getConnection().equals("sqlite"))
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(resultSet.getString(field));
        else
            return resultSet.getDate(field);
    }

    private static java.sql.Date convertUtilDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    private void createBanLogEntry(String action, Player player, int reason, int banPoints, Date from, Date until, boolean permanent, int staff) {
        try {

            //SQL
            @SuppressWarnings("SqlResolve")
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "INSERT INTO core_banlog (action, userid, reasonid, `from`, `until`, permanent, staff, `date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, action);
            ps.setInt(2, PlayerLoader.load(player).getId());
            ps.setInt(3, reason);
            ps.setDate(4, convertUtilDate(from));
            ps.setDate(5, convertUtilDate(until));
            ps.setBoolean(6, permanent);
            ps.setInt(7, staff);
            ps.executeUpdate();
            ps.close();

            // Discord
            SimpleDateFormat dtf = new SimpleDateFormat("HH:mm:ss");
            DiscordWebhook discordWebhook = new DiscordWebhook(
                    ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banlog_webhook")
            );

            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setAuthor("GalaxyCore » BanLog", "", "");
            embed.setTitle(StringUtils.firstLetterUppercase(action));
            embed.setFooter(dtf.format(new Date()), "");
            embed.setThumbnail("https://minotar.net/bust/" + player.getUsername() + "/190.png");
            embed.setDescription(quote(player.getUsername()));

            if (reason != -1)
                embed.addField("Grund:", String.valueOf(reason), false);

            if (banPoints != -1)
                embed.addField("Banpunkte:", String.valueOf(banPoints), false);

            //noinspection ConstantConditions
            if (from != null)
                embed.addField("Von:", dtf.format(from), false);

            //noinspection ConstantConditions
            if (until != null)
                embed.addField("Bis:", dtf.format(until), false);

            if (permanent)
                embed.addField("Permanent:", "Ja", false);
            else
                embed.addField("Permanent:", "Nein", false);

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

        } catch (Exception ignore) {
        }
    }

    private void createUnbanLogEntry(Player player, Player staff) {
        createBanLogEntry("unban", player, -1, 0, null, null, false, PlayerLoader.load(staff).getId());
    }

    private String quote(Object s) {
        return "`" + s.toString() + "`";
    }

    public String replaceRelevant(String s, int userID) {
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
            String reason = rsReason.getString("name");
            psReason.close();
            rsReason.close();

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
                    .replaceAll("%reason%", reason);

            rs.close();
            ps.close();

        } catch (Exception ignore) {
        }

        return s;
    }

}
