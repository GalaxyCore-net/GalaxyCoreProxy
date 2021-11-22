package net.galaxycore.galaxycoreproxy.joinme;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.TimeDelay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.*;

public class JoinMeCommand implements SimpleCommand {

    private static final HashMap<Player, String> joinMe = new HashMap<>();
    private static final HashMap<UUID, Long> joinMeCooldown = new HashMap<>();

    private final IPlayerManager playerManager;

    public JoinMeCommand() {
        ProxyProvider.getProxy().registerCommand(this, "joinme");
        playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("§cThis command is only available for players!"));
            return;
        }

        Player player = (Player) invocation.source();

        if (args.length >= 1) {
            Optional<Player> optionalJoinmeSender = ProxyProvider.getProxy().getServer().getPlayer(args[0]);
            if (optionalJoinmeSender.isEmpty()) {
                MessageUtils.sendI18NMessage(player, "proxy.command.joinme.joinme_not_found");
                return;
            }
            Player joinMeSender = optionalJoinmeSender.get();
            if (joinMe.get(joinMeSender) != null) {
                playerManager.getPlayerExecutor(player.getUniqueId()).connect(joinMe.get(joinMeSender));
            } else {
                MessageUtils.sendI18NMessage(player, "proxy.command.joinme.joinme_not_found");
            }
        } else {
            if (!player.hasPermission("proxy.command.joinme")) {
                MessageUtils.sendI18NMessage(player, "proxy.command.joinme.noperms");
                return;
            }

            if (player.getCurrentServer().isPresent() && player.getCurrentServer().get().getServerInfo().getName().toLowerCase().contains("lobby")) {
                MessageUtils.sendI18NMessage(player, "proxy.command.joinme.not_in_lobby");
                return;
            }

            if (joinMe.get(player) != null) {
                MessageUtils.sendI18NMessage(player, "proxy.command.joinme.joinme_exists");
                return;
            }

            if (!player.hasPermission("proxy.command.joinme.bypass")) {
                if (joinMeCooldown.containsKey(player.getUniqueId()))
                    if (joinMeCooldown.get(player.getUniqueId()) < System.currentTimeMillis())
                        joinMeCooldown.remove(player.getUniqueId());
                    else {
                        MessageUtils.sendI18NMessage(player, "proxy.command.joinme.in_cooldown");
                        return;
                    }
            }

            joinMe.put(player, player.getCurrentServer().get().getServerInfo().getName());
            for (Player onlinePlayer : ProxyProvider.getProxy().getServer().getAllPlayers()) {
                TextComponent message = Component.text(MessageUtils.getI18NMessage(onlinePlayer, "proxy.command.joinme.click_to_join"));
                message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/joinme" + player));
                message = message.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(MessageUtils.getI18NMessage(onlinePlayer,
                        "proxy.command.joinme.click_to_join"))));
                onlinePlayer.sendMessage(Component.text("\n" +
                        MessageUtils.getI18NMessage(onlinePlayer, "proxy.command.joinme.player_sent_joinme")
                                .replaceAll("%player%", player.getUsername())
                                .replaceAll("%server%", player.getCurrentServer().get().getServerInfo().getName())));
                onlinePlayer.sendMessage(message);
                onlinePlayer.sendMessage(Component.text("\n"));
            }

            TimeDelay delay = TimeDelay.readTimeDelay(ProxyProvider.getProxy().getProxyNamespace().get("proxy.joinme.delay"));
            TimeDelay cooldown = TimeDelay.readTimeDelay(ProxyProvider.getProxy().getProxyNamespace().get("proxy.joinme.cooldown"));
            ProxyProvider.getProxy().getServer().getScheduler().buildTask(ProxyProvider.getProxy(), () -> joinMe.remove(player))
                    .delay(delay.getDelay(), delay.getDelayUnit()).schedule();

            joinMeCooldown.put(player.getUniqueId(), System.currentTimeMillis() + cooldown.getMillis());
        }

    }

}
