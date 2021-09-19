package net.galaxycore.galaxycoreproxy.tabcompletion;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

public class TabCompletionListener {

    public TabCompletionListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @SuppressWarnings("UnstableApiUsage") // IDC
    @Subscribe
    public void onTabComplete(PlayerAvailableCommandsEvent event) {
        event.getRootNode().getChildren().removeIf((command) -> command.getName().contains(":"));
    }

}
