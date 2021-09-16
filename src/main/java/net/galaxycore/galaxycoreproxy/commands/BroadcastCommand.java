package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.RawCommand;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.kyori.adventure.text.Component;

public class BroadcastCommand implements RawCommand {

    private final GalaxyCoreProxy proxy;

    public BroadcastCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.registerCommand(this, "broadcast", "bc");
    }

    @Override
    public void execute(Invocation invocation) {
        proxy.getServer().sendMessage(Component.text("\n§6Broadcast: §4§l" + invocation.arguments() + "\n"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.broadcast");
    }

}
