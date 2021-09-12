package net.galaxycore.galaxycoreproxy.tabcompletion;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;

public class TabCompletionListener {

    public TabCompletionListener(GalaxyCoreProxy proxy) {
        proxy.registerListener(this);
    }

    @SuppressWarnings("UnstableApiUsage") // IDC
    @Subscribe
    public void onTabComplete(PlayerAvailableCommandsEvent event) {
        event.getRootNode().getChildren().removeIf((command) -> command.getName().contains(":"));
    }

}
