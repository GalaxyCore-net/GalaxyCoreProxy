package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class TeamCommand implements SimpleCommand {

    public TeamCommand() {
        ProxyProvider.getProxy().registerCommand(this, "team");
    }

    @Override
    public void execute(Invocation invocation) {
        AtomicInteger teamMemberCount = new AtomicInteger();
        StringBuilder bobTheBuilder = new StringBuilder();
        ProxyProvider.getProxy().getServer().getAllPlayers().stream().filter(player -> player.hasPermission("group.team")).forEach(player -> {
            teamMemberCount.getAndIncrement();
            bobTheBuilder.append(player.getUsername()).append("\n");
        });
        MessageUtils.sendMessage(invocation.source(), MessageUtils.getI18NMessage(invocation.source(), "proxy.command.team.team")
                + "§f: (§a" + teamMemberCount + "§f:\n" + bobTheBuilder);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }

}
