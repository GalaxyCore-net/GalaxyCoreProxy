package net.galaxycore.galaxycoreproxy.proxyPlayerControl;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.kyori.adventure.text.TextComponent;

public class PlayerDisconnectListener {

    public PlayerDisconnectListener() {
        ProxyProvider.getProxy().registerListener(this);
    }

    @Subscribe
    public void onPlayerDisconnect(KickedFromServerEvent event) {
        if(event.getServerKickReason().isPresent() && (event.getServerKickReason().get() instanceof TextComponent)) {
            TextComponent component = (TextComponent) event.getServerKickReason().get();
            if(component.content().equals("[ERR_RECON]")) {
                CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getPlayerExecutor(event.getPlayer().getUniqueId()).connect(event.getServer().getServerInfo().getName());
            }else if(component.content().contains("[ERR_RECON::")) {
                String serverNameToConnect = component.content().substring(12, (component.content().length() - 1));
                CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getPlayerExecutor(event.getPlayer().getUniqueId()).connect(serverNameToConnect);
            }
        }else {
            CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getPlayerExecutor(event.getPlayer().getUniqueId()).kick(MessageUtils.getI18NMessage(event.getPlayer(), "proxy.default_kick_reason"));
        }
    }

}
