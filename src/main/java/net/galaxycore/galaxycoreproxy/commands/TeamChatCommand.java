package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.RawCommand;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.LuckPermsAPIWrapper;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.StringUtils;

public class TeamChatCommand implements RawCommand {

    public TeamChatCommand() {
        ProxyProvider.getProxy().registerCommand(this, "teamchat", "tc");
    }

    @Override
    public void execute(Invocation invocation) {

        ProxyProvider.getProxy().getServer().getAllPlayers().stream()
                .filter(player -> player.hasPermission("proxy.command.teamchat"))
                .filter(player -> player.hasPermission("proxy.team.login"))
                .forEach(player -> {
                    LuckPermsAPIWrapper wrapper = new LuckPermsAPIWrapper(player);
                    MessageUtils.sendMessage(player,
                            StringUtils.replaceRelevant(MessageUtils.getI18NMessage(player,
                                    "proxy.command.teamchat.prefix"), wrapper) + invocation.arguments());
                });

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.teamchat");
    }

}
