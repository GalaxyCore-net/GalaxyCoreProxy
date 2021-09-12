package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.PrefixProvider;
import net.kyori.adventure.text.Component;

public class HelpCommand implements SimpleCommand {

    private final GalaxyCoreProxy proxy;

    public HelpCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.registerCommand(this, "help");
    }

    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(Component.text(PrefixProvider.getPrefix() + "§6Information"));
        invocation.source().sendMessage(Component.text(proxy.getProxyNamespace().get("proxy.help")));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.help");
    }

}
