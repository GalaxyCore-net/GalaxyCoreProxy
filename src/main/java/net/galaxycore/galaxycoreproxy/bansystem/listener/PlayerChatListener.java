package net.galaxycore.galaxycoreproxy.bansystem.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

import java.util.HashMap;

public class PlayerChatListener {

    public PlayerChatListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @Getter
    private static final HashMap<Player, String> lastMessage = new HashMap<>();

    @Subscribe
    public void onMessage(PlayerChatEvent event) {

        lastMessage.put(event.getPlayer(), event.getMessage());

        if (PlayerLoader.load(event.getPlayer()).isMuted()) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            MessageUtils.sendI18NMessage(event.getPlayer(), "proxy.ban.muted.chat");
        }

    }

}
