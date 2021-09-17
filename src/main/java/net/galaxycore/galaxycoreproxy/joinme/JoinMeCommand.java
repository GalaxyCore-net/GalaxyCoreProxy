package net.galaxycore.galaxycoreproxy.joinme;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.utils.TimeDelay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.*;

public class JoinMeCommand implements SimpleCommand {

    private static final HashMap<Player, String> joinMe = new HashMap<>();
    private static final List<UUID> joinMeCooldown = new ArrayList<>();

    private final GalaxyCoreProxy proxy;
    private final IPlayerManager playerManager;

    public JoinMeCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.registerCommand(this, "joinme");
        playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if(!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("§cThis command is only available for players!"));
            return;
        }

        Player player = (Player) invocation.source();

        if(args.length >= 1) {
            Optional<Player> optionalJoinmeSender = proxy.getServer().getPlayer(args[0]);
            if(optionalJoinmeSender.isEmpty()) {
                player.sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.joinme_not_found")));
                return;
            }
            Player joinMeSender = optionalJoinmeSender.get();
            if(joinMe.get(joinMeSender) != null) {
                playerManager.getPlayerExecutor(player.getUniqueId()).connect(joinMe.get(joinMeSender));
            }else {
                player.sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.joinme_not_found")));
            }
        }else {
            if(!player.hasPermission("proxy.command.joinme")) {
                player.sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.noperms")));
                return;
            }

            if(player.getCurrentServer().isPresent() && player.getCurrentServer().get().getServerInfo().getName().toLowerCase().contains("lobby")) {
                player.sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.not_in_lobby")));
                return;
            }

            if(joinMe.get(player) != null) {
                player.sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.joinme_exists")));
                return;
            }

            if(!player.hasPermission("proxy.command.joinme.bypass")){
                joinMeCooldown.add(player.getUniqueId());
                if(joinMeCooldown.contains(player.getUniqueId()))
                    player.sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.in_cooldown")));
                    return;
            }

            joinMe.put(player, player.getCurrentServer().get().getServerInfo().getName());
            for(Player onlinePlayer : proxy.getServer().getAllPlayers()) {
                TextComponent message = Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.click_to_join"));
                //noinspection ResultOfMethodCallIgnored i think it doesn´t matter
                message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/joinme" + player));
                //noinspection ResultOfMethodCallIgnored i think it doesn´t matter
                message.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(I18N.getByLang("de_DE", "proxy.command.joinme.click_to_join"))));
                onlinePlayer.sendMessage(Component.text("\n" +
                        I18N.getByLang("de_DE", "proxy.command.joinme.player_sent_joinme")
                                        .replaceAll("%player%", player.getUsername())
                                        .replaceAll("%server%", player.getCurrentServer().get().getServerInfo().getName())));
                onlinePlayer.sendMessage(message);
                onlinePlayer.sendMessage(Component.text("\n"));
            }

            TimeDelay delay = TimeDelay.readTimeDelay(proxy, "proxy.joinme.delay");
            TimeDelay cooldown = TimeDelay.readTimeDelay(proxy, "proxy.joinme.cooldown");
            proxy.getServer().getScheduler().buildTask(proxy, () -> joinMe.remove(player)).delay(delay.getDelay(), delay.getDelayUnit()).schedule();

            if(player.hasPermission("proxy.command.joinme.bypass"))
                return;
            proxy.getServer().getScheduler().buildTask(proxy, () -> joinMeCooldown.remove(player.getUniqueId())).delay(cooldown.getDelay(), cooldown.getDelayUnit()).schedule();

        }

    }

}
