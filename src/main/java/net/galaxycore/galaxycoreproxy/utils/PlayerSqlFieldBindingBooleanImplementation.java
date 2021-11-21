package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.sql.PreparedStatement;

public class PlayerSqlFieldBindingBooleanImplementation extends PlayerSqlFieldBinding<Boolean> {
    public PlayerSqlFieldBindingBooleanImplementation(Player player, String field) {
        super(player, field);
    }

    @Override
    public Boolean getValue() {
        throw new RuntimeException("Not Implemented");
    }

    @SneakyThrows
    @Override
    public PlayerSqlFieldBindingBooleanImplementation updateValue(Boolean value) {
        PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("UPDATE core_playercache SET " + getField() + "=? WHERE uuid=?");
        ps.setBoolean(1, value);
        ps.setString(2, getPlayerLoader().getUuid().toString());
        ps.executeUpdate();

        setPlayerLoader(PlayerLoader.loadNew(getPlayer()));
        return this;
    }
}
