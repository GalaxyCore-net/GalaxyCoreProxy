package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.PlayerSqlFieldBindingBooleanImplementation;
import net.kyori.adventure.text.Component;

@Getter
public class SocialSpyCommand implements SimpleCommand {
    public SocialSpyCommand() {
        ProxyProvider.getProxy().registerCommand(this, "socialspy", "sspy", "ss");
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("proxy.command.socialspy")) {
            notify(invocation, "proxy.command.socialspy.no_permissions");
            return;
        }

        if (PlayerLoader.load((Player) invocation.source()).isSocialSpy()) {
            new PlayerSqlFieldBindingBooleanImplementation((Player) invocation.source(), "socialspy").updateValue(false);
            notify(invocation, "proxy.command.socialspy.off");
            return;
        }

        new PlayerSqlFieldBindingBooleanImplementation((Player) invocation.source(), "socialspy").updateValue(true);
        notify(invocation, "proxy.command.socialspy.on");
    }

    private void notify(Invocation invocation, String key) {
        invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), key)));
    }
}
