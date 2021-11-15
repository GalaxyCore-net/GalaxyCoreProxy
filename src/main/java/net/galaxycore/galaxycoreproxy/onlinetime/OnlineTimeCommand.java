package net.galaxycore.galaxycoreproxy.onlinetime;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.SuggestionUtils;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public class OnlineTimeCommand implements SimpleCommand {
    public OnlineTimeCommand() {
        ProxyProvider.getProxy().registerCommand(this, "onlinetime", "ot", "time");
    }

    /**
     * Executes the command for the specified invocation.
     *
     * @param invocation the invocation context
     */
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        /*
         * In the following code, I repeat the float division. This is because of the exactness of the end value of it
         * Just don't bother with that, it won't work without it. And do me a favor: Don't remove it.
         */

        if (args.length == 0) {
            // Player is invocation.source()
            long onlineTime = ProxyProvider.getProxy().getOnlineTime().getInterpolator()
                    .interpolate((Player) invocation.source()); /* 60000ms = 1m */

            invocation.source().sendMessage(Component.text(
                    MessageUtils.getI18NMessage(invocation.source(), "proxy.command.onlinetime")
                            .replace("%h%", "" + (int) Math.floor((onlineTime / 60000F) / 60F)) /* 60000ms = 1m */
                            .replace("%m%", "" + (int) Math.floor((onlineTime / 60000F) % 60 )) /* 60000ms = 1m */
            ));
        } else {
            // Player is args[0]
            Optional<Player> targetOptional = ProxyProvider.getProxy().getServer().getPlayer(args[0]);

            if (targetOptional.isEmpty()) {
                MessageUtils.sendI18NMessage(invocation.source(), "proxy.onlinetime.player404");
                return;
            }

            Player target = targetOptional.get();

            long onlineTime = ProxyProvider.getProxy().getOnlineTime().getInterpolator()
                    .interpolate(target);

            invocation.source().sendMessage(Component.text(
                    MessageUtils.getI18NMessage(invocation.source(), "proxy.command.onlinetime.other")
                            .replace("%player%", target.getUsername())
                            .replace("%h%", "" + (int) Math.floor((onlineTime / 60000F) / 60F)) /* 60000ms = 1m */
                            .replace("%m%", "" + (int) Math.floor((onlineTime / 60000F) % 60 )) /* 60000ms = 1m */
            ));
        }
    }

    /**
     * Provides tab complete suggestions for the specified invocation.
     *
     * @param invocation the invocation context
     * @return the tab complete suggestions
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        return SuggestionUtils.suggestPlayerFirst(invocation);
    }
}
