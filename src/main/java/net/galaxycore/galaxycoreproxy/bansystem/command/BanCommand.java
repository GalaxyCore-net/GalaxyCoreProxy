package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.ArrayList;
import java.util.List;

public class BanCommand implements SimpleCommand {

    public BanCommand() {
        ProxyProvider.getProxy().registerCommand(this, "ban");
    }

    ///ban <player> <reasonID>
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(args.length == 0) {
            PunishmentReason.sendReasonsToAudience(invocation.source(), "ban");
        }else if(args.length == 1) {
            BanSystemProvider.getBanSystem().getBanManager().banPlayer(args[0], invocation.source() instanceof Player ? (Player) invocation.source() : null);
        }else {
            BanSystemProvider.getBanSystem().getBanManager().banPlayer(args[0], args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null);
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.ban");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> ret = new ArrayList<>();

        if (invocation.arguments().length == 1) {
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> ret.add(player.getUsername()));
        }

        if (invocation.arguments().length == 2) {
            for (Integer reasonID : PunishmentReason.getReasonHashMap().keySet()) {
                PunishmentReason reason = PunishmentReason.getReasonHashMap().get(reasonID);
                if (invocation.source().hasPermission(reason.getRequiredPermissionBan())) {
                    ret.add(String.valueOf(reason.getId()));
                }
            }
        }

        return ret;
    }

}
