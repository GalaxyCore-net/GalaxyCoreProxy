package net.galaxycore.galaxycoreproxy.friends;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.*;
import java.util.stream.Collectors;

public class FriendCommand implements SimpleCommand {
    private final HashMap<PlayerLoader, PlayerLoader> targetSourceFriendRequests = new HashMap<>();

    public FriendCommand() {
        ProxyProvider.getProxy().registerCommand(this, "friend", "f");
    }

    /**
     * Executes the command for the specified invocation.
     *
     * @param invocation the invocation context
     */
    @SneakyThrows
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
            else {
                StringBuilder newFriends = new StringBuilder();
                for (PlayerLoader friend : friends) {
                    newFriends.append(friend.getLastName()).append(" ");
                }
                friendsList = friendsList.replace("{friends}", newFriends.toString());
            }

            player.sendMessage(Component.text(friendsList));
        } else if (args[0].equalsIgnoreCase("msg")) {
            if (args.length <= 2) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.msg.help");
                return;
            }

            ImmutableList<PlayerLoader> friends = ProxyProvider.getProxy().getFriendManager().getFriends(PlayerLoader.load(player));

            Optional<PlayerLoader> optionalFriendLoader = friends.stream().filter(playerLoader -> playerLoader.getLastName().equalsIgnoreCase(args[1])).findFirst();

            if (optionalFriendLoader.isEmpty()) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.friendnotfound");
                return;
            }

            PlayerLoader friendLoader = optionalFriendLoader.get();

            Optional<Player> optionalFriend = ProxyProvider.getProxy().getServer().getPlayer(friendLoader.getUuid());

            if (optionalFriend.isEmpty()) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.notonline");
                return;
            }

            Player friend = optionalFriend.get();

            ArrayList<String> argsList = new ArrayList<>(List.of(args));
            argsList.remove(0);
            argsList.remove(0);

            String msg = String.join(" ", argsList);

            friend.sendMessage(Component.text(MessageUtils.getI18NMessage(friend, "proxy.command.friend.msg.target").replace("{msg}", msg).replace("{player}", player.getUsername())));
            player.sendMessage(Component.text(MessageUtils.getI18NMessage(player, "proxy.command.friend.msg.source").replace("{msg}", msg).replace("{target}", friend.getUsername())));
        } else if (args[0].equalsIgnoreCase("deny")) {
            if (!targetSourceFriendRequests.containsKey(PlayerLoader.load(player))) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.deny.notexists");
                return;
            }

            targetSourceFriendRequests.remove(PlayerLoader.load(player));

            MessageUtils.sendI18NMessage(player, "proxy.command.friend.deny.ok");
        } else if (args[0].equalsIgnoreCase("accept")) {
            PlayerLoader playerLoader = PlayerLoader.load(player);

            if (!targetSourceFriendRequests.containsKey(playerLoader)) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.accept.notexists");
                return;
            }

            PlayerLoader friendLoader = targetSourceFriendRequests.get(playerLoader);
            targetSourceFriendRequests.remove(playerLoader);

            ProxyProvider.getProxy().getFriendManager().addFriend(playerLoader, friendLoader);

            MessageUtils.sendI18NMessage(player, "proxy.command.friend.accept.ok");

            Optional<Player> optionalFriend = ProxyProvider.getProxy().getServer().getPlayer(friendLoader.getUuid());

            if (optionalFriend.isEmpty()) {
                return;
            }

            Player friend = optionalFriend.get();

            MessageUtils.sendMessage(friend, MessageUtils.getI18NMessage(friend, "proxy.command.friend.accept.ok.other").replace("{player}", player.getUsername()));
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 2) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.add.help");
                return;
            }

            PlayerLoader playerLoader = PlayerLoader.load(player);

            Optional<Player> optionalTarget = ProxyProvider.getProxy().getServer().getPlayer(args[1]);

            if (optionalTarget.isEmpty()) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.notonline");
                return;
            }

            Player target = optionalTarget.get();

            targetSourceFriendRequests.put(PlayerLoader.load(target), playerLoader);

            player.sendMessage(Component.text(MessageUtils.getI18NMessage(player, "proxy.command.friend.add.ok")));
            target.sendMessage(Component.text(MessageUtils.getI18NMessage(target, "proxy.command.friend.add.request").replace("{player}", player.getUsername())));
            target.sendMessage(Component.text("")
                    .append(Component.text(MessageUtils.getI18NMessage(target, "proxy.command.friend.add.yes")).clickEvent(ClickEvent.runCommand("/f accept")))
                    .append(Component.text(" "))
                    .append(Component.text(MessageUtils.getI18NMessage(target, "proxy.command.friend.add.no")).clickEvent(ClickEvent.runCommand("/f deny")))
            );
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.remove.help");
                return;
            }

            PlayerLoader playerLoader = PlayerLoader.load(player);

            Optional<PlayerLoader> optionalTarget = PlayerLoader.load(args[1]);

            if (optionalTarget.isEmpty()) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.notonline");
                return;
            }

            PlayerLoader targetLoader = optionalTarget.get();

            if (ProxyProvider.getProxy().getFriendManager().getFriends(playerLoader).stream().anyMatch(anonymousTarget -> Objects.equals(anonymousTarget.getLastName(), args[0]))) {
                MessageUtils.sendI18NMessage(player, "proxy.command.friend.friendnotfound");
                return;
            }

            ProxyProvider.getProxy().getFriendManager().removeFriend(playerLoader, targetLoader);

            player.sendMessage(Component.text(MessageUtils.getI18NMessage(player, "proxy.command.friend.remove.ok").replace("{player}", targetLoader.getLastName())));

            Optional<Player> optionalFriend = ProxyProvider.getProxy().getServer().getPlayer(targetLoader.getUuid());

            if (optionalFriend.isEmpty()) {
                return;
            }

            Player friend = optionalFriend.get();

            MessageUtils.sendMessage(friend, MessageUtils.getI18NMessage(friend, "proxy.command.friend.remove.other").replace("{player}", player.getUsername()));
        } else
            sendHelp(player);
    }

    /**
     * For Nico
     * <i>he needs it</i>
     *
     * @param player The player to send help to
     */
    private void sendHelp(Player player) {
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.title");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.list");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.msg");
        MessageUtils.sendI18NMessage(player, "proxy.command.friend.help.add");
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
                "remove",
                "add"
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
