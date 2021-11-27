package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

public class UnmuteCommand implements SimpleCommand {

    public UnmuteCommand() {
        ProxyProvider.getProxy().registerCommand(this, "unmute");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if (args.length == 0)
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.unmute.usage");
        else
            BanSystemProvider.getBanSystem().getBanManager().unmutePlayer(args[0], invocation.source() instanceof Player ? ((Player) invocation.source()).getUsername() : "Console");

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.unmute");
    }

}
