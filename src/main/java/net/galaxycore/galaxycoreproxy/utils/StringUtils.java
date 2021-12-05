package net.galaxycore.galaxycoreproxy.utils;

import com.velocitypowered.api.proxy.Player;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;

import java.text.SimpleDateFormat;

public class StringUtils {

    public static String firstLetterUppercase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String replaceRelevant(String input, LuckPermsAPIWrapper wrapper) {
        input = input.replace("%player%", wrapper.getPlayerName());
        input = input.replace("%rank_displayname%", wrapper.getPermissionDisplayName());
        input = input.replace("%rank_color%", wrapper.getPermissionColor());
        input = input.replace("%rank_prefix%", wrapper.getPermissionsPrefix());
        input = input.replace("%rank_name%", wrapper.getPermissionsGroupNameRaw());
        input = input.replace("%chat_important%", wrapper.isChatImportant() ? "§c" : "");
        return input;
    }

    public static String replacePlayerLoader(String input, PlayerLoader loader) {
        return input.replace("{id}", String.valueOf(loader.getId()))
                .replace("{uuid}", loader.getUuid().toString())
                .replace("{name}", loader.getLastName())
                .replace("{first_login}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(loader.getFirstlogin()))
                .replace("{last_login}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(loader.getLastLogin()))
                .replace("{last_daily_reward}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(loader.getLastDailyReward()))
                .replace("{ban_points}", String.valueOf(loader.getBanPoints()))
                .replace("{mute_points}", String.valueOf(loader.getMutePoints()))
                .replace("{warn_points}", String.valueOf(loader.getWarnPoints()))
                .replace("{reports}", String.valueOf(loader.getReports()))
                .replace("{team_login}", loader.isTeamLogin() ? "True" : "False")
                .replace("{debug}", loader.isDebug() ? "True" : "False")
                .replace("{socialspy}", loader.isSocialSpy() ? "True" : "False")
                .replace("{commandspy}", loader.isCommandSpy() ? "True" : "False")
                .replace("{vanished}", loader.isVanished() ? "True" : "False")
                .replace("{nicked}", loader.isNicked() ? "True" : "False")
                .replace("{lastnick}", String.valueOf(loader.getLastNick()))
                .replace("{coins}", String.valueOf(loader.getCoins()))
                .replace("{banned}", loader.isBanned() ? "True" : "False")
                .replace("{muted}", loader.isMuted() ? "True" : "False")
                .replace("{onlinetime}", "%h%m")
                .replace("{bans}", String.valueOf(loader.getBans()))
                .replace("{mutes}", String.valueOf(loader.getMutes()))
                .replace("{warns}", String.valueOf(loader.getWarns()));
    }

    public static String replacePlayerLoader(String input, Player player) {
        return replacePlayerLoader(input, PlayerLoader.load(player));
    }
}
