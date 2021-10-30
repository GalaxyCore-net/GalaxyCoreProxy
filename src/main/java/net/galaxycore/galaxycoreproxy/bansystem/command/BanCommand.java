package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
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
            String reasonDisplay = MessageUtils.getI18NMessage(invocation.source(), "proxy.command.ban.reason_list");
            PunishmentReason.loadReasons();
            PunishmentReason.getReasonHashMap().forEach((id, reason) -> MessageUtils.sendMessage(invocation.source(), reasonDisplay
                    .replaceAll("%id%", String.valueOf(reason.getId()))
                    .replaceAll("%name%", reason.getName())
                    .replaceAll("%req_permission_warn%", reason.getRequiredPermissionWarn())
                    .replaceAll("%req_permission_mute%", reason.getRequiredPermissionMute())
                    .replaceAll("%req_permission_ban%", reason.getRequiredPermissionBan())
                    .replaceAll("%points%", String.valueOf(reason.getPoints()))
                    .replaceAll("%points_increase_percent%", String.valueOf(reason.getPointsIncreasePercent()))
                    .replaceAll("%duration%", String.valueOf(reason.getDuration()))
                    .replaceAll("%permanent%", String.valueOf(reason.isPermanent()))));
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

}
