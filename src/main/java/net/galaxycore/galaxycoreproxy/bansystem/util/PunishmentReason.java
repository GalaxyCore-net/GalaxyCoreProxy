package net.galaxycore.galaxycoreproxy.bansystem.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.audience.Audience;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@Getter
@AllArgsConstructor
public class PunishmentReason {

    @Getter
    private static final HashMap<Integer, PunishmentReason> reasonHashMap = new HashMap<>();

    private int id;
    private String name;
    private String requiredPermissionWarn, requiredPermissionMute, requiredPermissionBan;
    private int points, pointsIncreasePercent;
    private int duration;
    private boolean permanent;

    @SneakyThrows
    public static PunishmentReason loadReason(int id) {
        if (reasonHashMap.containsKey(id))
            return reasonHashMap.get(id);

        PreparedStatement load = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "SELECT * FROM core_punishment_reasons WHERE id=?"
        );
        load.setInt(1, id);
        ResultSet loadResult = load.executeQuery();

        if(!loadResult.next()) {
            loadResult.close();
            load.close();
            if(id == Integer.parseInt(ProxyProvider.getProxy().getProxyNamespace().get("proxy.ban.default_reason")))
                return new PunishmentReason(1, "Fehler", "ban.admin", "ban.admin", "ban.admin", 1, 0, 0, true);
            else
                return loadReason(Integer.parseInt(ProxyProvider.getProxy().getProxyNamespace().get("proxy.ban.default_reason")));
        }

        PunishmentReason reason = new PunishmentReason(
                id,
                loadResult.getString("name"),
                loadResult.getString("required_permission_warn"),
                loadResult.getString("required_permission_mute"),
                loadResult.getString("required_permission_ban"),
                loadResult.getInt("points"),
                loadResult.getInt("points_increase_percent"),
                loadResult.getInt("duration"),
                loadResult.getBoolean("permanent")
        );

        loadResult.close();
        load.close();

        reasonHashMap.put(id, reason);

        return reason;

    }

    @SneakyThrows
    public static void loadReasons() {
        PreparedStatement all = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("" +
                "SELECT * FROM core_punishment_reasons"
        );
        ResultSet allResult = all.executeQuery();
        while((allResult.next())) {
            loadReason(allResult.getInt("id"));
        }
        allResult.close();
        all.close();
    }

    @SneakyThrows
    public static void registerDefaultReason(String name, String requiredPermissionMute, String requiredPermissionBan, int points, int pointsIncreasePercent, int duration, boolean permanent) {
        if(isReasonExists(name))
            return;

        PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "INSERT INTO core_punishment_reasons (`name`, required_permission_warn, required_permission_mute, required_permission_ban, points, points_increase_percent, duration, permanent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );
        update.setString(1, name);
        update.setString(2, "ban.admin");
        update.setString(3, requiredPermissionMute);
        update.setString(4, requiredPermissionBan);
        update.setInt(5, points);
        update.setInt(6, pointsIncreasePercent);
        update.setInt(7, duration);
        update.setBoolean(8, permanent);
        update.executeUpdate();
        update.close();

    }

    public static void registerDefaultReason(String name, String permission, int points, int pointsIncreasePercent, int duration, boolean permanent) {
        registerDefaultReason(name, permission, permission, points, pointsIncreasePercent, duration, permanent);
    }

    @SneakyThrows
    public static boolean isReasonExists(String name) {
        PreparedStatement reason = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                "SELECT * FROM core_punishment_reasons WHERE name=?"
        );
        reason.setString(1, name);
        ResultSet rsReason = reason.executeQuery();
        boolean hasNext = rsReason.next();
        rsReason.close();
        reason.close();
        return hasNext;
    }

    public static void sendReasonsToAudience(Audience source, String scope) {
        loadReasons();
        String reasonDisplay = MessageUtils.getI18NMessage(source, "proxy.command." + scope + ".reason_list");
        reasonHashMap.forEach((id, reason) -> MessageUtils.sendMessage(source, reasonDisplay
                .replaceAll("%id%", String.valueOf(reason.getId()))
                .replaceAll("%name%", reason.getName())
                .replaceAll("%req_permission_warn%", reason.getRequiredPermissionWarn())
                .replaceAll("%req_permission_mute%", reason.getRequiredPermissionMute())
                .replaceAll("%req_permission_ban%", reason.getRequiredPermissionBan())
                .replaceAll("%points%", String.valueOf(reason.getPoints()))
                .replaceAll("%points_increase_percent%", String.valueOf(reason.getPointsIncreasePercent()))
                .replaceAll("%duration%", String.valueOf(reason.getDuration()))
                .replaceAll("%permanent%", String.valueOf(reason.isPermanent()))));
    }

}
