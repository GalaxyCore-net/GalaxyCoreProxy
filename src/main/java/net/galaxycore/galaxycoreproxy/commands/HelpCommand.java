package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.PrefixProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.text.Component;

public class HelpCommand implements SimpleCommand {

    public HelpCommand(GalaxyCoreProxy proxy) {
        proxy.registerCommand(this, "help");
    }

    @Override
    public void execute(Invocation invocation) {
        invocation.source().sendMessage(Component.text(PrefixProvider.getPrefix() + "§6Information"));
        invocation.source().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.help")));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.help");
    }

}
