package net.galaxycore.galaxycoreproxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

public class PluginCommandListener {

    public PluginCommandListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @Subscribe
    public void onPlayerExecutePluginCommand(CommandExecuteEvent event) {
        if(ProxyProvider.getProxy().getPluginCommand().getCommands().contains(event.getCommand())) {
            if(event.getCommandSource().hasPermission("velocity.command.plugins"))
                event.setResult(CommandExecuteEvent.CommandResult.command("velocity plugins"));
            else
                event.getCommandSource().sendMessage(Component.text("§fProxyPlugins (9): "
                        + MessageUtils.getI18NMessage(event.getCommandSource(), "proxy.command.plugins.no_permission")));
        }

    }

}
