package net.galaxycore.galaxycoreproxy.bansystem.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

            event.setResult(ResultedEvent.ComponentResult.denied(BanSystemProvider.getBanSystem().getBanManager().buildBanScreen(event.getPlayer(), rsBan.getBoolean("permanent"))));
            psBan.close();
            rsBan.close();
        }

        if (hasVPN(event.getPlayer())) {
            event.setResult(ResultedEvent.ComponentResult.denied(BanSystemProvider.getBanSystem().getBanManager().buildVPNScreen(event.getPlayer())));
        }

    }

    private boolean hasVPN(Player player) {
        try {
            URL obj = new URL("https://proxycheck.io/v2/" + player.getRemoteAddress().getHostString() + "/?key=318n07-0o7054-y9y82a-75o3hr");
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            StringBuilder bobTheBuilder = new StringBuilder();
            String inputLine;
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((inputLine = in.readLine()) != null)
                bobTheBuilder.append(inputLine);
            in.close();
            return bobTheBuilder.toString().contains("yes");
        } catch (Exception e) {
            return false;
        }

    }

}
