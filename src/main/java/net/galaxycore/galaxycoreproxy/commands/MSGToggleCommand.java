package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

@Getter
public class MSGToggleCommand implements SimpleCommand {
    public MSGToggleCommand() {
        ProxyProvider.getProxy().registerCommand(this, "msgtoggle");
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("proxy.command.lockmsg")) {
            notify(invocation, "proxy.command.msgtoggle.no_permissions");
            return;
        }

        if (invocation.source().hasPermission("proxy.command.msg.lock")) {
            ProxyProvider.getProxy().getServer().getCommandManager().executeAsync(
                    ProxyProvider.getProxy().getServer().getConsoleCommandSource(),
                    "lpv user " + ((Player) invocation.source()).getUsername() + " permission set proxy.command.msg.lock false"
            );

            notify(invocation, "proxy.command.msgtoggle.off");
            return;
        }

        ProxyProvider.getProxy().getServer().getCommandManager().executeAsync(
                ProxyProvider.getProxy().getServer().getConsoleCommandSource(),
                "lpv user " + ((Player) invocation.source()).getUsername() + " permission set proxy.command.msg.lock true"
        );

        notify(invocation, "proxy.command.msgtoggle.on");
    }

    private void notify(Invocation invocation, String key) {
        invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), key)));
    }
}
