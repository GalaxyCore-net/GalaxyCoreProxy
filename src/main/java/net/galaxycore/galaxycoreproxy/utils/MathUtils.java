package net.galaxycore.galaxycoreproxy.utils;

public class MathUtils {

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }catch (NumberFormatException e) {
            return false;
        }
    }

}
