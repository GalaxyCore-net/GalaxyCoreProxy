package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.StringUtils;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerinfoCommand implements SimpleCommand {

    public PlayerinfoCommand() {
        ProxyProvider.getProxy().registerCommand(this, "playerinfo", "pi" /*Maths Professor*/);
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
            long onlineTime = ProxyProvider.getProxy().getOnlineTime().getInterpolator()
                    .interpolate(target);
            invocation.source().sendMessage(Component.text(
                    StringUtils.replacePlayerLoader(MessageUtils.getI18NMessage(invocation.source(), "proxy.command.playerinfo.info_for_player"), target)
                            .replace("{server}", target.getCurrentServer().isPresent() ? target.getCurrentServer().get().getServerInfo().getName() : "§c404 not Found")
                            .replace("{onlinetime}", Math.floor((onlineTime / 60000F) / 60F) + "h " + Math.floor((onlineTime / 60000F) % 60) + "m")));

        } else {
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.playerinfo.usage");
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
        return invocation.source().hasPermission("proxy.command.playerinfo");
    }

}
