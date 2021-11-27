package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IPCommand implements SimpleCommand {

    public IPCommand() {
        ProxyProvider.getProxy().registerCommand(this, "ip");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if (args.length >= 1) {

            Optional<Player> optionalPlayer = ProxyProvider.getProxy().getServer().getPlayer(args[0]);
            if (optionalPlayer.isEmpty()) {
                MessageUtils.sendI18NMessage(invocation.source(), "proxy.player_404");
                return;
            }
            Player target = optionalPlayer.get();
            invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), "proxy.command.ip.ip_of_player")
                    .replace("{ip}", target.getRemoteAddress().getHostString())
                    .replace("{player}", target.getUsername())));

        } else {
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.ip.usage");
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> ret = new ArrayList<>();

        if (invocation.arguments().length == 1) {
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> ret.add(player.getUsername()));
        }

        return ret;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.ip");
    }

}
