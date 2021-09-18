package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.text.Component;

import java.util.concurrent.atomic.AtomicInteger;

public class TeamCommand implements SimpleCommand {

    private final GalaxyCoreProxy proxy;

    public TeamCommand(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        proxy.registerCommand(this, "team");
    }

    @Override
    public void execute(Invocation invocation) {
        AtomicInteger teamMemberCount = new AtomicInteger();
        StringBuilder bobTheBuilder = new StringBuilder();
        proxy.getServer().getAllPlayers().stream().filter(player -> player.hasPermission("group.team")).forEach(player -> {
            teamMemberCount.getAndIncrement();
            bobTheBuilder.append(player.getUsername()).append("\n");
        });
        invocation.source().sendMessage(Component.text(I18N.getByLang("de_DE", "proxy.command.team.team") + "§f: (§a" + teamMemberCount + "§f):\n" + bobTheBuilder));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }

}
