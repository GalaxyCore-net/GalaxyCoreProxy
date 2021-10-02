package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

public class BanCommand implements SimpleCommand {

    public BanCommand() {
        ProxyProvider.getProxy().registerCommand(this, "ban");
    }

    ///ban <player> <reasonID>
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(args.length == 0) {
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.ban.too_few_args");
        }else if(args.length == 1) {
            //TODO: Do something with the return value
            System.out.println(BanSystemProvider.getBanSystem().getBanManager().banPlayer(args[0], invocation.source() instanceof Player ? (Player) invocation.source() : null));
        }else {
            //TODO: Do something with the return value
            System.out.println(BanSystemProvider.getBanSystem().getBanManager().banPlayer(args[0], args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null));
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.ban");
    }

}
