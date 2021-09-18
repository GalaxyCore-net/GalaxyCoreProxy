package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

public class PluginCommand implements SimpleCommand {

    private final GalaxyCoreProxy proxy;

    public PluginCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.registerCommand(this, "plugins", "pl");
    }

    @Override
    public void execute(Invocation invocation) {

        if(invocation.source().hasPermission("velocity.command.plugins")) {
//            StringBuilder bobTheBuilder = new StringBuilder();
//            AtomicInteger pluginCount = new AtomicInteger();
//            proxy.getServer().getPluginManager().getPlugins().forEach(plugin -> {
//                pluginCount.getAndIncrement();
//                //noinspection OptionalGetWithoutIsPresent there is no way around
//                bobTheBuilder.append(plugin.getDescription().getName().get()).append("§f, §a");
//            });
//            invocation.source().sendMessage(Component.text("§fPlugins (" + pluginCount + "): §a" + bobTheBuilder));
            proxy.getServer().sendMessage((Identity) invocation.source(), Component.text("/velocity plugins"));
        }else {
            invocation.source().sendMessage(Component.text("§fProxyPlugins (9): "+ I18N.getByLang("de_DE", "proxy.command.plugins.no_permission")));
        }

    }

}
