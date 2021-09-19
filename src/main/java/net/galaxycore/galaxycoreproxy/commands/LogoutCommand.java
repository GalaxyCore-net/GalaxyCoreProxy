package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.PermissionUtils;
import net.kyori.adventure.text.Component;

import java.sql.PreparedStatement;

public class LogoutCommand implements SimpleCommand {

    public LogoutCommand() {
        ProxyProvider.getProxy().registerCommand(this, "logout");
    }

    @Override
    public void execute(Invocation invocation) {

        if(!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("§cThis command can only be executed by a Player"));
            return;
        }

        Player player = (Player) invocation.source();

        if(player.hasPermission("proxy.team.login")) {
            logoutPlayer(player);
        }else {
            MessageUtils.sendI18NMessage(player, "proxy.command.logout.not_logged_in");
        }

    }

    @SneakyThrows
    private void logoutPlayer(Player player) {
        PermissionUtils.setPermission(player, "proxy.team.login", false, ProxyProvider.getProxy());
        MessageUtils.sendI18NMessage(player, "proxy.command.logout.logged_out");
        PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("UPDATE core_playercache SET teamlogin=FALSE WHERE uuid=?");
        ps.setString(1, player.getUniqueId().toString());
        ps.executeUpdate();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.login");
    }

}
