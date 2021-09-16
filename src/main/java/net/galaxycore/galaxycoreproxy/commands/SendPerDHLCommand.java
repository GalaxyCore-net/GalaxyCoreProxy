package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SendPerDHLCommand implements SimpleCommand {

    private final IPlayerManager playerManager;
    private final GalaxyCoreProxy proxy;

    public SendPerDHLCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        playerManager = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class);
        proxy.registerCommand(this, "sendperdhl", "spd", "send");
    }

    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        switch (args.length) {
            case 0:
                StringBuilder bobTheBuilder = new StringBuilder("§aServer: (").append(proxy.getServer().getAllServers().size()).append(")§f:\n");
                proxy.getServer().getAllServers().forEach(server -> bobTheBuilder.append(server.getServerInfo().getName()).append("\n"));
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
                    invocation.source().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.sendperdhl.server_not_found")));
                }
                break;
            case 2:
                Optional<Player> optionalTarget = proxy.getServer().getPlayer(args[0]);
                if(optionalTarget.isEmpty()) {
                    invocation.source().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.sendperdhl.target_not_found")));
                    return;
                }
                Player target = optionalTarget.get();
                try {
                    playerManager.getPlayerExecutor(target.getUniqueId()).connect(args[1]);
                }catch (Exception e) {
                    invocation.source().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.sendperdhl.server_not_found")));
                }
            default:
                invocation.source().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.sendperdhl.wrong_usage")));
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
            proxy.getServer().getAllPlayers().forEach(player -> finalRet.add(player.getUsername()));
        }else if(args.length == 2) {
            ret = new ArrayList<>();
            List<String> finalRet = ret;
            proxy.getServer().getAllServers().forEach(server -> finalRet.add(server.getServerInfo().getName()));
        }

        return ret;
    }
}
