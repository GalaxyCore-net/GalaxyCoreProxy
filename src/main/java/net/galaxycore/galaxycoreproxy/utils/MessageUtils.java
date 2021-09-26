package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.Arrays;

public class MessageUtils {

    public static void sendI18NMessage(Audience source, String key) {
        source.sendMessage(Component.text(getI18NMessage(source, key)));
    }

    public static String getI18NMessage(Audience source, String key) {
        boolean unchanged = true;
        String message = "§cMessage not found";
        if(source instanceof Player) {
            message = I18N.getByPlayer((Player) source, key);
            unchanged = false;
        }
        if(message.equals("§cMessage not found") && unchanged)
            return I18N.getByLang("en_GB", key);
        return message;
    }

    public static void sendMessage(Audience source, String... messages) {
        StringBuilder bobTheBuilder = new StringBuilder();
        Arrays.stream(messages).forEach(bobTheBuilder::append);
        source.sendMessage(Component.text(bobTheBuilder.toString()));
    }

    public static void broadcastI18NMessage(String key) {
        ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> player.sendMessage(Component.text("\n" + getI18NMessage(player, key) + "\n")));
    }

}
