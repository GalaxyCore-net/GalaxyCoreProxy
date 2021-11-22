package net.galaxycore.galaxycoreproxy.utils;

import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;

public class MathUtils {

    public static boolean isInt(String s) {
        ProxyProvider.getProxy().getLogger().info(s);
        try {
            Integer.parseInt(s);
            return true;
        }catch (NumberFormatException e) {
            return false;
        }
    }

}
