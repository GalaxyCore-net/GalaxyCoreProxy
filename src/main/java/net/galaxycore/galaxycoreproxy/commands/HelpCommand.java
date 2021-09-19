package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.PrefixProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.text.Component;

public class HelpCommand implements SimpleCommand {

    public HelpCommand() {
        ProxyProvider.getProxy().registerCommand(this, "help");
    }

    @Override
    public void execute(Invocation invocation) {
        if(invocation.source() instanceof Player)
            invocation.source().sendMessage(Component.text(PrefixProvider.getPrefix() + I18N.getByPlayer((Player) invocation.source(), "proxy.command.help")));
        else
            invocation.source().sendMessage(Component.text("§cThis command is only available for Players!"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.help");
    }

}
