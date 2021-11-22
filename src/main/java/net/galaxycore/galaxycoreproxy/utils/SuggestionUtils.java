package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.command.SimpleCommand;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

import java.util.ArrayList;
import java.util.List;

public class SuggestionUtils {
    public static List<String> suggestPlayerFirst(SimpleCommand.Invocation invocation) {
        final ArrayList<String> suggestions = new ArrayList<>();

        if(invocation.arguments().length == 0)
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> suggestions.add(player.getUsername()));

        if(invocation.arguments().length == 1)
            ProxyProvider.getProxy().getServer().getAllPlayers().stream().filter(player -> player.getUsername().contains(invocation.arguments()[0])).forEach(player -> suggestions.add(player.getUsername()));

        return suggestions;
    }
}
