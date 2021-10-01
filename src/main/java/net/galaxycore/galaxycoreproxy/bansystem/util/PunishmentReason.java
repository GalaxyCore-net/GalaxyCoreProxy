package net.galaxycore.galaxycoreproxy.bansystem.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@Getter
@AllArgsConstructor
public class PunishmentReason {

    @Getter
    private static final HashMap<Integer, PunishmentReason> reasonHashMap = new HashMap<>();

    public int id;
    public String name;
    public String requiredPermissionWarn, requiredPermissionMute, requiredPermissionBan;
    public int points, points_increase_percent;
    public int duration;
    public boolean permanent;

    @SneakyThrows
    public static PunishmentReason loadReason(int id) {
        if(reasonHashMap.containsKey(id))
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

}
