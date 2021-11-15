package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.Setter;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;

@Getter
public abstract class PlayerSqlFieldBinding<T> {
    private final Player player;
    private final String field;

    @Setter
    private PlayerLoader playerLoader;

    public PlayerSqlFieldBinding(Player player, String field) {
        this.playerLoader = PlayerLoader.load(player);
        this.player = player;
        this.field = field;
    }

    public abstract T getValue();
    public abstract PlayerSqlFieldBinding<T> updateValue(T value);
}
