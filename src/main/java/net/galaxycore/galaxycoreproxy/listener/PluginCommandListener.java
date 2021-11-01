package net.galaxycore.galaxycoreproxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

public class PluginCommandListener {

    public PluginCommandListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @Subscribe
    public void onPlayerExecutePluginCommand(CommandExecuteEvent event) {
        // Check if Command checks plguins to hand down to Server
        if(ProxyProvider.getProxy().getPluginCommand().getCommands().contains(event.getCommand())) {
            if(event.getCommandSource().hasPermission("velocity.command.plugins"))
                event.setResult(CommandExecuteEvent.CommandResult.command("velocity plugins"));
            else
                event.getCommandSource().sendMessage(Component.text("§fProxyPlugins (9): "
                        + MessageUtils.getI18NMessage(event.getCommandSource(), "proxy.command.plugins.no_permission")));
        }

        //IMPORTANT: BELOW THIS IF STATEMENT YOU CAN ONLY PROCESS EVENTS WHERE THE EXECUTOR HAS TO BE A PLAYER
        if(!(event.getCommandSource() instanceof Player)) {
            event.getCommandSource().sendMessage(Component.text("§cThis command is only available for Players"));
            return;
        }

        // Check if Player has accepted rules or not
        if(PlayerLoader.load((Player) event.getCommandSource()) == null) {
            if(!event.getCommand().startsWith("rules"))
                event.setResult(CommandExecuteEvent.CommandResult.denied());
        }else {
            if(event.getCommand().startsWith("rules"))
                event.setResult(CommandExecuteEvent.CommandResult.denied());
        }

    }

}
