package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.PermissionUtils;
import net.kyori.adventure.text.Component;

import java.sql.PreparedStatement;

public class LoginCommand implements SimpleCommand {


    public LoginCommand() {
        ProxyProvider.getProxy().registerCommand(this, "login");
    }

    @Override
    public void execute(Invocation invocation) {

        if(!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("§cThis command can only be executed by a Player"));
            return;
        }

        Player player = (Player) invocation.source();

        if(player.hasPermission("proxy.team.login")) {
            MessageUtils.sendI18NMessage(player, "proxy.command.login.already_logged_in");
        }else {
            loginPlayer(player);
        }

    }

    @SneakyThrows
    private void loginPlayer(Player player) {
        PermissionUtils.setPermission(player, "proxy.team.login", true, ProxyProvider.getProxy());
        MessageUtils.sendI18NMessage(player, "proxy.command.login.logged_in");
        PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("UPDATE core_playercache SET teamlogin=TRUE WHERE uuid=?");
        ps.setString(1, player.getUniqueId().toString());
        ps.executeUpdate();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.login");
    }

}
