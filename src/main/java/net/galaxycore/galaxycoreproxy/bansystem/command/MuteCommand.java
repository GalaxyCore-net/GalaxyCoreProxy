package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.bansystem.listener.PlayerChatListener;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.ArrayList;
import java.util.List;

public class MuteCommand implements SimpleCommand {

    public MuteCommand() {
        ProxyProvider.getProxy().registerCommand(this, "mute");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(args.length == 0) {
            PunishmentReason.sendReasonsToAudience(invocation.source(), "mute");
        }else if(args.length == 1) {
            BanSystemProvider.getBanSystem().getBanManager().mutePlayer(args[0], invocation.source() instanceof Player ? (Player) invocation.source() : null, ProxyProvider.getProxy().getServer().getPlayer(args[0]).isPresent() && PlayerChatListener.getLastMessage().containsKey(ProxyProvider.getProxy().getServer().getPlayer(args[0]).get()) ? PlayerChatListener.getLastMessage().get(ProxyProvider.getProxy().getServer().getPlayer(args[0]).get()) : "");
        }else if(args.length == 2){
            BanSystemProvider.getBanSystem().getBanManager().mutePlayer(args[0], args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null, ProxyProvider.getProxy().getServer().getPlayer(args[0]).isPresent() && PlayerChatListener.getLastMessage().containsKey(ProxyProvider.getProxy().getServer().getPlayer(args[0]).get()) ? PlayerChatListener.getLastMessage().get(ProxyProvider.getProxy().getServer().getPlayer(args[0]).get()) : "");
        }else {
            StringBuilder bobTheBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                bobTheBuilder.append(args[i]).append(" ");
            }
            BanSystemProvider.getBanSystem().getBanManager().mutePlayer(args[0], args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null, bobTheBuilder.toString());
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.mute");
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
                if (invocation.source().hasPermission(reason.getRequiredPermissionMute())) {
                    ret.add(String.valueOf(reason.getId()));
                }
            }
        }

        return ret;
    }

}
