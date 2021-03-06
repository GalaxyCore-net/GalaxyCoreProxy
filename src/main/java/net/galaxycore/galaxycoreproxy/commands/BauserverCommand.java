package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MathUtils;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

public class BauserverCommand implements SimpleCommand {

    private final IPlayerManager playerManager;

    public BauserverCommand() {
        ProxyProvider.getProxy().registerCommand(this, "bauserver");
        playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
    }

    @Override
    public void execute(Invocation invocation) {

        if(!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("§cThis command is only available for Players!"));
            return;
        }

        Player player = (Player) invocation.source();

        playerManager.getPlayerExecutor(player.getUniqueId()).connect(getBauserverServerName(invocation.arguments(), player));

    }

    public String getBauserverServerName(String[] args, CommandSource source) {

        String index = "1";
        if(args.length >= 1) {
            if(!MathUtils.isInt(args[0])) {
                MessageUtils.sendI18NMessage(source, "proxy.command.bauserver.int_required");
            }else {
                index = args[0];
            }
        }

        return "Bauserver-" + index;

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.bauserver");
    }

}
