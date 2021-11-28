package net.galaxycore.galaxycoreproxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener {
    private final Pattern reg;

    public ChatListener() {
        ProxyProvider.getProxy().registerListener(this);

        reg = Pattern.compile("(" + ProxyProvider.getProxy().getProxyNamespace().get("forbiddenWords") + ")", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        event.setResult(PlayerChatEvent.ChatResult.message(censor(event.getMessage())));
    }

    private String censor(String message) {
        Matcher matcher = reg.matcher(message);
        return matcher.replaceAll("***");
    }
}
