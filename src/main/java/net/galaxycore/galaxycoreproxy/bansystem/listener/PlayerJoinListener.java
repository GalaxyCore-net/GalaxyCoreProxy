package net.galaxycore.galaxycoreproxy.bansystem.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class PlayerJoinListener {

    public PlayerJoinListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @SneakyThrows
    @Subscribe
    public void onPlayerJoin(LoginEvent event) {

        if(BanSystemProvider.getBanSystem().getBanManager().isPlayerBanned(event.getPlayer().getUsername())) {

            PreparedStatement psBan = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                    "SELECT * FROM core_bans WHERE userid=?"
            );
            psBan.setInt(1, PlayerLoader.load(event.getPlayer()).getId());
            ResultSet rsBan = psBan.executeQuery();
            if(!rsBan.next())
                return;

            Timestamp now = new Timestamp(new Date().getTime());
            if(rsBan.getTimestamp("until").before(now) && !rsBan.getBoolean("permanent")) {
                PreparedStatement update = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement(
                        "DELETE FROM core_bans WHERE userid=?"
                );
                update.setInt(1, PlayerLoader.load(event.getPlayer()).getId());
                update.executeUpdate();
                update.close();
                rsBan.close();
                psBan.close();
                return;
            }
            rsBan.close();
            psBan.close();

            event.setResult(ResultedEvent.ComponentResult.denied(BanSystemProvider.getBanSystem().getBanManager().buildBanScreen(event.getPlayer())));

        }

    }

}
