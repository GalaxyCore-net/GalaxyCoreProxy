package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.bansystem.BanSystemProvider;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ReportCommand implements SimpleCommand {

    public ReportCommand() {
        ProxyProvider.getProxy().registerCommand(this, "report");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if (args.length == 0) {
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.report.usage");
        } else if (args.length == 1) {
            BanSystemProvider.getBanSystem().getBanManager().reportPlayer(args[0], invocation.source() instanceof Player ? (Player) invocation.source() : null);
        } else {

            if (args[0].equalsIgnoreCase("claim")) {
                //report claim <player>
                if (invocation.source().hasPermission("proxy.command.report.claim"))
                    BanSystemProvider.getBanSystem().getBanManager().claimReport(args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null);
                else
                    MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.report.no_permission");
            } else if (args[0].equalsIgnoreCase("deny")) {
                //report deny <player>
                if (invocation.source().hasPermission("proxy.command.report.deny"))
                    BanSystemProvider.getBanSystem().getBanManager().denyReport(args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null);
                else
                    MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.report.no_permission");
            } else if (args[0].equalsIgnoreCase("close")) {
                //report close <player>
                if (invocation.source().hasPermission("proxy.command.report.close"))
                    BanSystemProvider.getBanSystem().getBanManager().closeReport(args[1], invocation.source() instanceof Player ? (Player) invocation.source() : null);
                else
                    MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.report.no_permission");
            } else {
                StringBuilder bobTheBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++)
                    bobTheBuilder.append(args[i]).append(" ");
                BanSystemProvider.getBanSystem().getBanManager().reportPlayer(args[0], bobTheBuilder.toString(), invocation.source() instanceof Player ? (Player) invocation.source() : null);
            }

        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> ret = new ArrayList<>();

        if (invocation.arguments().length == 1) {
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> ret.add(player.getUsername()));
            if (invocation.source().hasPermission("proxy.command.report.claim"))
                ret.add("claim");
            if (invocation.source().hasPermission("proxy.command.report.deny"))
                ret.add("deny");
            if (invocation.source().hasPermission("proxy.command.report.close"))
                ret.add("close");
        }

        if (invocation.arguments().length == 2) {
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> ret.add(player.getUsername()));
        }

        return ret;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.report");
    }

}
