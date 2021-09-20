package net.galaxycore.galaxycoreproxy.tabcompletion;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.Arrays;
import java.util.List;

public class TabCompletionListener {

    public TabCompletionListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @SuppressWarnings("UnstableApiUsage") // IDC
    @Subscribe
    public void onTabComplete(PlayerAvailableCommandsEvent event) {
        event.getRootNode().getChildren().removeIf((command) -> command.getName().contains(":"));
        if(PlayerLoader.load(event.getPlayer()) != null)
            event.getRootNode().getChildren().removeIf(command -> command.getName().equals("rules"));

        List<String> blackListedCommands = Arrays.asList(ProxyProvider.getProxy().getProxyNamespace().get("proxy.commandblacklist").toLowerCase().split("\\|"));
        event.getRootNode().getChildren().removeIf(command -> blackListedCommands.contains(command.getName().toLowerCase())
                && !event.getPlayer().hasPermission("proxy.commandblacklist.bypass"));
    }

}
