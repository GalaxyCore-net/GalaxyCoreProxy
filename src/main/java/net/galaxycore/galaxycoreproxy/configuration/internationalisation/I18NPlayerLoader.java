package net.galaxycore.galaxycoreproxy.configuration.internationalisation;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class I18NPlayerLoader {

    private final GalaxyCoreProxy proxy;

    @Getter
    @Setter
    public static I18NPlayerLoader playerloaderInstance;

    @Getter
    private final HashMap<Player, String> languages = new HashMap<>();

    public I18NPlayerLoader(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.getServer().getEventManager().register(proxy, this);
    }

    @SneakyThrows
    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        Player player = event.getPlayer();
        PlayerLoader playerLoader = PlayerLoader.loadNew(player);

        if(playerLoader == null) {
            return;
        }

        PreparedStatement loadLanguage = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT language_id FROM I18N_player_data WHERE id=?");
        loadLanguage.setInt(1, playerLoader.getId());
        ResultSet loadResult = loadLanguage.executeQuery();

        if(!loadResult.next()) {
            loadResult.close();
            loadLanguage.close();
            return;
        }

        AtomicReference<String> lang = new AtomicReference<>("");

        I18N.getInstanceRef().get().getLanguages().forEach((s, minecraftLocale) -> {
            try {
                if(loadResult.getInt("language_id") == minecraftLocale.getId()) {
                    lang.set(s);
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        if(!lang.get().equals(""))
            languages.put(player, lang.get());

        loadResult.close();
        loadLanguage.close();
        
        ProxyProvider.getProxy().getOnlineTime().getInterpolator().registerJoin(event.getPlayer());
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        languages.remove(event.getPlayer());
    }

    public static String getLocale(Player player) {
        return playerloaderInstance.getLanguages().getOrDefault(player, "en_GB");
    }

}
