package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.SuggestionUtils;
import net.kyori.adventure.text.Component;

import java.util.*;

public class MSGCommand implements SimpleCommand {
    @Getter
    private static final HashMap<String, String> lastConversationPartner = new HashMap<>();

    public MSGCommand() {
        ProxyProvider.getProxy().registerCommand(this, "msg");
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if(!invocation.source().hasPermission("proxy.command.msg")) {
            fail(invocation, "proxy.command.msg.noperms");
            return;
        }

        if (args.length < 2) {
            fail(invocation, "proxy.command.msg.usage");
            return;
        }

        Optional<Player> optionalPlayerRecv = ProxyProvider.getProxy().getServer().getPlayer(args[0]);
        if (optionalPlayerRecv.isEmpty()) {
            fail(invocation, "proxy.command.msg.player_not_found");
            return;
        }

        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Player playerRecv = optionalPlayerRecv.get();
        Player playerFrom = (Player) invocation.source();

        String recvTransmission = MessageUtils.getI18NMessage(playerRecv, "proxy.command.msg.transmission");
        String fromTransmission = MessageUtils.getI18NMessage(playerFrom, "proxy.command.msg.transmission");

        String recvYou = MessageUtils.getI18NMessage(playerRecv, "proxy.command.msg.you");
        String fromYou = MessageUtils.getI18NMessage(playerFrom, "proxy.command.msg.you");

        recvTransmission = recvTransmission.replaceAll("\\{p1}", playerFrom.getUsername());
        recvTransmission = recvTransmission.replaceAll("\\{p2}", recvYou);
        recvTransmission = recvTransmission.replaceAll("\\{msg}", text);

        fromTransmission = fromTransmission.replaceAll("\\{p1}", fromYou);
        fromTransmission = fromTransmission.replaceAll("\\{p2}", playerRecv.getUsername());
        fromTransmission = fromTransmission.replaceAll("\\{msg}", text);

        if(playerRecv.hasPermission("proxy.command.msg.lock")) {
            if (!playerFrom.hasPermission("proxy.command.msg.lock.bypass")){
                fail(invocation, "proxy.command.msg.locked");
                return;
            }
        }

        lastConversationPartner.put(playerFrom.getUsername(), playerRecv.getUsername());
        lastConversationPartner.put(playerRecv.getUsername(), playerFrom.getUsername());

        playerRecv.sendMessage(Component.text(recvTransmission));
        playerFrom.sendMessage(Component.text(fromTransmission));

        for (Player player : ProxyProvider.getProxy().getServer().getAllPlayers()) {
            System.out.println(player.getUsername());
            if (player == playerFrom) continue;
            if (player == playerRecv) continue;
            if (PlayerLoader.load(player) == null) continue;
            if (!PlayerLoader.load(player).isSocialSpy()) continue;
            if (!PlayerLoader.load(player).isTeamLogin()) continue;
            if(playerFrom.hasPermission("proxy.command.socialspy.block") && !player.hasPermission("proxy.command.socialspy.block.bypass")) continue;
            if(playerRecv.hasPermission("proxy.command.socialspy.block") && !player.hasPermission("proxy.command.socialspy.block.bypass")) continue;
            if (!player.hasPermission("proxy.command.socialspy")) continue;

            player.sendMessage(Component.text(
                    MessageUtils.getI18NMessage(player, "proxy.command.msg.transmission")
                            .replace("{p1}", playerFrom.getUsername())
                            .replace("{p2}", playerRecv.getUsername())
                            .replace("{msg}", text)
            ));
        }
    }

    private void fail(Invocation invocation, String key) {
        invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), key)));
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
