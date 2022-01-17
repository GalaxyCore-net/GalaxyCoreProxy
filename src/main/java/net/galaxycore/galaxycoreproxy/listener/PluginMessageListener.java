package net.galaxycore.galaxycoreproxy.listener;

import com.google.common.io.ByteArrayDataInput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginMessageListener {

    public PluginMessageListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        ByteArrayDataInput data = event.dataAsDataStream();

        int id = data.readInt();

        if (id == 64) { // Run Command
            Player player = (Player) event.getSource();

            StringBuilder bobTheBuffer = new StringBuilder();

            int len = data.readInt();

            for (int i = 0; i < len; i++) {
                bobTheBuffer.append(data.readChar());
            }

            ProxyProvider.getProxy().getServer().getCommandManager().executeAsync(player, bobTheBuffer.toString());
        }
    }
}