package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand implements SimpleCommand {

    public UnbanCommand() {
        ProxyProvider.getProxy().registerCommand(this, "unban");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(args.length == 0)
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.unban.too_few_args");
        else
            BanSystemProvider.getBanSystem().getBanManager().unbanPlayer(args[0], invocation.source() instanceof Player ? ((Player) invocation.source()).getUsername() : "Console");

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.unban");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> ret = new ArrayList<>();

        if (invocation.arguments().length == 1) {
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> ret.add(player.getUsername()));
        }

        return ret;
    }
}
