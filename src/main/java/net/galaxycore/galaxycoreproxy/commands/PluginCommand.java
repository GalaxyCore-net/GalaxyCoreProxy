package net.galaxycore.galaxycoreproxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.ArrayList;
import java.util.List;

public class PluginCommand implements SimpleCommand {

    @Getter
    private final List<String> commands = new ArrayList<>();

    public PluginCommand() {
        commands.add("plugins");
        commands.add("pl");
        ProxyProvider.getProxy().registerCommand(this, "plugins", "pl");
    }

    @Override
    public void execute(Invocation invocation) {
        //This is just so that the command is registered, it will be handled in the listener
    }

}
