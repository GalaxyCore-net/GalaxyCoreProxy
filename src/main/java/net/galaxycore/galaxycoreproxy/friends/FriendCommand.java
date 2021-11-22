package net.galaxycore.galaxycoreproxy.friends;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.jsoup.internal.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FriendCommand implements SimpleCommand {
    public FriendCommand() {
        ProxyProvider.getProxy().registerCommand(this, "friend", "f");
    }

    /**
     * Executes the command for the specified invocation.
     *
     * @param invocation the invocation context
     */
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        Player player = (Player) invocation.source();

        if (args.length == 0) {
            sendHelp(player);
        } else if (args[0].equalsIgnoreCase("list")) {
            String friendsList = MessageUtils.getI18NMessage(player, "proxy.command.friend.list");

            ImmutableList<PlayerLoader> friends = ProxyProvider.getProxy().getFriendManager().getFriends(PlayerLoader.load(player));

            if (friends.size() == 0)
                friendsList = friendsList.replace("{friends}", MessageUtils.getI18NMessage(player, "proxy.command.friend.list.none"));
            else{
                StringBuilder newFriends = new StringBuilder();
                for (PlayerLoader friend : friends) {
                    newFriends.append(friend.getLastName()).append(" ");
                }
                friendsList = friendsList.replace("{friends}", newFriends.toString());
            }

            player.sendMessage(Component.text(friendsList));
        }
    }

    /**
     * For Flo
     * <i>he needs it</i>
     *
     * @param player The player to send help to
     */
    private void sendHelp(Player player) {
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.title");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.list");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.msg");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.accept");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.accept");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.deny");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.remove");
    }

    /**
     * Provides tab complete suggestions for the specified invocation.
     *
     * @param invocation the invocation context
     * @return the tab complete suggestions
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        ArrayList<String> suggestions = new ArrayList<>();

        String[] args = invocation.arguments();

        List<String> standardArgs = Arrays.asList(
                "list",
                "msg",
                "accept",
                "deny",
                "remove"
        );

        if (args.length == 0)
            suggestions.addAll(standardArgs);
        else if (args.length == 1)
            suggestions.addAll(standardArgs.stream().filter(s -> s.contains(args[0])).collect(Collectors.toList()));
        else if (args.length == 2)
            for (Player player : ProxyProvider.getProxy().getServer().getAllPlayers())
                suggestions.add(player.getUsername());

        else if (args.length == 3 && Arrays.asList("msg", "add", "remove").contains(args[0]))
            for (Player player : ProxyProvider.getProxy().getServer().getAllPlayers())
                if (args[1].contains(player.getUsername()))
                    suggestions.add(player.getUsername());


        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.friend");
    }
}
