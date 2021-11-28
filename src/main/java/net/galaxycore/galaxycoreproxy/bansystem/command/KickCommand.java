package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class KickCommand implements SimpleCommand {

    public KickCommand() {
        ProxyProvider.getProxy().registerCommand(this, "kick");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(args.length == 0) {
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.kick.too_few_args");
        }else if(args.length == 1) {
            BanSystemProvider.getBanSystem().getBanManager().kickPlayer(args[0], invocation.source() instanceof Player ? (Player) invocation.source() : null);
        }else {
            StringBuilder bobTheBuilder = new StringBuilder();
            for(int i = 1; i < args.length; i++)
                bobTheBuilder.append(args[i]).append(" ");
            BanSystemProvider.getBanSystem().getBanManager().kickPlayer(args[0], bobTheBuilder.toString(), invocation.source() instanceof Player ? (Player) invocation.source() : null);
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.kick");
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
