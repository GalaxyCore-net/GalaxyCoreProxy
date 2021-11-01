package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

public class MuteCommand implements SimpleCommand {

    public MuteCommand() {
        ProxyProvider.getProxy().registerCommand(this, "mute");
    }

    //TODO: Tab Completion
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(args.length == 0) {
            PunishmentReason.sendReasonsToAudience(invocation.source(), "mute");
        }else if(args.length == 1) {
            BanSystemProvider.getBanSystem().getBanManager().mutePlayer(args[0], invocation.source() instanceof Player ? (Player) invocation.source() : null);
        }else {
            BanSystemProvider.getBanSystem().getBanManager().mutePlayer(args[0], args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null);
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.mute");
    }

}
