package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.RawCommand;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.text.Component;

public class BroadcastCommand implements RawCommand {

    public BroadcastCommand() {
        ProxyProvider.getProxy().registerCommand(this, "broadcast", "bc");
    }

    @Override
    public void execute(Invocation invocation) {
        ProxyProvider.getProxy().getServer().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.broadcast") + invocation.arguments() + "\n"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.broadcast");
    }

}
