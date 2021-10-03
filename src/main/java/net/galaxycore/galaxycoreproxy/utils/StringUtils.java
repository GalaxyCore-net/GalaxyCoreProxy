package net.galaxycore.galaxycoreproxy.utils;

public class StringUtils {

    public static String firstLetterUppercase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
  
    public static String replaceRelevant(String input, LuckPermsAPIWrapper wrapper) {
        input = input.replaceAll("%player%", wrapper.getPlayerName());
        input = input.replaceAll("%rank_displayname%", wrapper.getPermissionDisplayName());
        input = input.replaceAll("%rank_color%", wrapper.getPermissionColor());
        input = input.replaceAll("%rank_prefix%", wrapper.getPermissionsPrefix());
        input = input.replaceAll("%rank_name%", wrapper.getPermissionsGroupNameRaw());
        input = input.replaceAll("%chat_important%", wrapper.isChatImportant() ? "§c" : "");
        return input;
    }
}
