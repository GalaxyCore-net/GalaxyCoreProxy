package net.galaxycore.galaxycoreproxy.bansystem.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class PlayerJoinListener {

    public PlayerJoinListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent event) {

        if(BanSystemProvider.getBanSystem().getBanManager().isPlayerBanned(event.getPlayer()))
            event.setResult(ResultedEvent.ComponentResult.denied(
                    Component.text(
                            BanSystemProvider.getBanSystem().getBanManager().replaceRelevant(
                                    MessageUtils.getI18NMessage(event.getPlayer(), "proxy.bansystem.banscreen_text"),
                                    PlayerLoader.load(event.getPlayer()).getId())
                    ).clickEvent(ClickEvent.clickEvent(
                            ClickEvent.Action.OPEN_URL,
                            ProxyProvider.getProxy().getProxyNamespace().get("proxy.bansystem.banscreen_url")
                    ))
            ));

    }

}
