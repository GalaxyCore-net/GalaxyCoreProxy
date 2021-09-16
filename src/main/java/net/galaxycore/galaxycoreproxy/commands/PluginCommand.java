package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.kyori.adventure.text.Component;

import java.util.concurrent.atomic.AtomicInteger;

public class PluginCommand implements SimpleCommand {

    private final GalaxyCoreProxy proxy;

    public PluginCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.registerCommand(this, "plugins", "pl");
    }

    @Override
    public void execute(Invocation invocation) {

        if(invocation.source().hasPermission("proxy.command.pl")) {
            StringBuilder bobTheBuilder = new StringBuilder();
            AtomicInteger pluginCount = new AtomicInteger();
            proxy.getServer().getPluginManager().getPlugins().forEach(plugin -> {
                pluginCount.getAndIncrement();
                //noinspection OptionalGetWithoutIsPresent there is no way around
                bobTheBuilder.append(plugin.getDescription().getName().get()).append("§f, §a");
            });
            invocation.source().sendMessage(Component.text("§fPlugins (" + pluginCount + "): §a" + bobTheBuilder));
        }else {
            invocation.source().sendMessage(Component.text("§fPlugins (9): §aHmm§f, §aich§f, §aglaube§f, §adass§f, §adu§f, §ahier§f, §anichts§f, §afinden§f, §awirst."));
        }

    }

}
