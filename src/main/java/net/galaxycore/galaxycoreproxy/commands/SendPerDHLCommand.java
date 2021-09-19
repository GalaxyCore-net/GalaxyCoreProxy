package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SendPerDHLCommand implements SimpleCommand {

    private final IPlayerManager playerManager;

    public SendPerDHLCommand() {
        playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
        ProxyProvider.getProxy().registerCommand(this, "sendperdhl", "spd", "send");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        switch (args.length) {
            case 0:
                StringBuilder bobTheBuilder = new StringBuilder(MessageUtils.getI18NMessage(invocation.source(), "proxy.command.sendperdhl.server")
                        + "§f: (§a" + ProxyProvider.getProxy().getServer().getAllServers().size() + ")§f:\n");
                ProxyProvider.getProxy().getServer().getAllServers().forEach(server -> bobTheBuilder.append(server.getServerInfo().getName()).append("\n"));
                invocation.source().sendMessage(Component.text(bobTheBuilder.toString()));
                break;
            case 1:
                if(!(invocation.source() instanceof Player)) {
                    invocation.source().sendMessage(Component.text("§cPlease use /spd <Player> <TargetServer>"));
                    return;
                }
                try  {
                    playerManager.getPlayerExecutor(((Player)invocation.source()).getUniqueId()).connect(args[0]);
                }catch (Exception e) {
                    MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.sendperdhl.server_not_found");
                }
                break;
            case 2:
                Optional<Player> optionalTarget = ProxyProvider.getProxy().getServer().getPlayer(args[0]);
                if(optionalTarget.isEmpty()) {
                    MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.sendperdhl.target_not_found");
                    return;
                }
                Player target = optionalTarget.get();
                try {
                    playerManager.getPlayerExecutor(target.getUniqueId()).connect(args[1]);
                }catch (Exception e) {
                    MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.sendperdhl.server_not_found");
                }
            default:
                if(invocation.source() instanceof Player)
                    invocation.source().sendMessage(Component.text(I18N.getByPlayer((Player) invocation.source(), "proxy.command.sendperdhl.wrong_usage")));
                else
                    invocation.source().sendMessage(Component.text(I18N.getByLang("en_GB", "proxy.command.sendperdhl.wrong_usage")));
                MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.sendperdhl.wrong_usage");
                break;
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.send");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> ret = new ArrayList<>();

        if(args.length == 1) {
            List<String> finalRet = ret;
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> finalRet.add(player.getUsername()));
        }else if(args.length == 2) {
            ret = new ArrayList<>();
            List<String> finalRet = ret;
            ProxyProvider.getProxy().getServer().getAllServers().forEach(server -> finalRet.add(server.getServerInfo().getName()));
        }

        return ret;
    }
}
