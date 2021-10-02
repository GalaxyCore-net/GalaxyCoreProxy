package net.galaxycore.galaxycoreproxy.bansystem;

import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycorecore.utils.DiscordWebhook;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MathUtils;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.StringUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                ps.setInt(7, PlayerLoader.load(staff).getId());
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_playercache SET banpoints=banpoints+? WHERE id=?"
                );
                update.setInt(1, banPoints);
                update.setInt(2, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createBanLogEntry("ban", player.getUsername(), reason, banPoints, from, until, permanent, staff.getUsername());
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
                ps.setInt(6, PlayerLoader.load(staff).getId());
                ps.setInt(6, PlayerLoader.load(player).getId());
                ps.executeUpdate();
                ps.close();

                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "UPDATE core_playercache SET banpoints=banpoints+? WHERE id=?"
                );
                update.setInt(1, banPoints);
                update.setInt(2, PlayerLoader.load(player).getId());
                update.executeUpdate();
                update.close();

                createBanLogEntry("ban", player.getUsername(), reason, banPoints, from, until, permanent, staff.getUsername());
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean banPlayer(Player player, int reason, Player staff) {
        try {

            if(player.hasPermission("group.team") && !staff.hasPermission("ban.admin") && !player.hasPermission("ban.admin")) {
                return false;
            }

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

            if(!rsIncomingBan.next()) {
                System.out.println("Incoming ban not found");
                return false;
            }
            int basePointsToAdd = rsIncomingBan.getInt("points");
            int bansForSameReason = 0;
            if(rsExistingBansForSameReason.next())
                while(rsExistingBansForSameReason.next())
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
        if (!MathUtils.isInt(reason)){
            System.out.println("Reason isn´t an int");
            return false;
        }

        Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(name);

        if (optionalPlayer.isEmpty()) {
            System.out.println("Player not found");
            return false;
        }

        Player player = optionalPlayer.get();
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

    @SneakyThrows
    public int getPlayerID(String playerName) {
        PreparedStatement stmt = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "SELECT * FROM core_playercache WHERE lastname=?"
        );
        stmt.setString(1, playerName);
        ResultSet rs = stmt.executeQuery();
        if(rs.next())
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

    private void createBanLogEntry(String action, String player, int reason, int banPoints, Date from, Date until, boolean permanent, String staff) {
        try {

            int playerid = getPlayerID(player);

            int staffid = getPlayerID(staff);

            //SQL
            @SuppressWarnings("SqlResolve")
            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "INSERT INTO core_banlog (action, userid, reasonid, `from`, `until`, permanent, staff, `date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, action);
            ps.setInt(2, playerid);
            ps.setInt(3, reason);
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

            if (reason != -1)
                embed.addField("Grund:", PunishmentReason.loadReason(reason).getName(), false);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUnbanLogEntry(String player, String staff) {
        createBanLogEntry("unban", player, -1, 0, null, null, false, staff);
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
            psReason.close();
            rsReason.close();
            PunishmentReason reason = PunishmentReason.loadReason(rs.getInt("reason"));

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
                    .replaceAll("%reason%", reason.getName());

            rs.close();
            ps.close();

        } catch (Exception ignore) {}

        return s;
    }

    public Component buildBanScreen(Player player) {
        return Component.text(
                BanSystemProvider.getBanSystem().getBanManager().replaceRelevant(
                        MessageUtils.getI18NMessage(player, "proxy.bansystem.banscreen_text"),
                        PlayerLoader.load(player).getId())
        ).clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.OPEN_URL,
                ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url")
        ));
    }

}
