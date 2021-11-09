package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

import java.util.List;

@Getter
public class RCommand implements SimpleCommand {
    public RCommand() {
        ProxyProvider.getProxy().registerCommand(this, "r");
    }

    @Override
    public void execute(Invocation invocation) {
        if (!MSGCommand.getLastConversationPartner().containsKey(((Player) invocation.source()).getUsername())) {
            fail(invocation);
            return;
        }

        ProxyProvider.getProxy().getServer().getCommandManager().executeAsync(invocation.source(), "msg " + MSGCommand.getLastConversationPartner().get(((Player) invocation.source()).getUsername()) + " " + String.join(" ", invocation.arguments()));
    }

    private void fail(Invocation invocation) {
        invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), "proxy.command.r.notfound")));
    }
}
