package net.galaxycore.galaxycoreproxy.bansystem.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.bansystem.util.PunishmentReason;
import net.galaxycore.galaxycoreproxy.configuration.PlayerLoader;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MathUtils;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.StringUtils;
import net.kyori.adventure.text.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class HistoryCommand implements SimpleCommand {

    public HistoryCommand() {
        ProxyProvider.getProxy().registerCommand(this, "history");
    }

    @SneakyThrows
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        //Load Bans
        if (args.length < 1) {
            MessageUtils.sendI18NMessage(invocation.source(), "proxy.command.history.usage");
        } else {
            Optional<Player> optionalTarget = ProxyProvider.getProxy().getServer().getPlayer(args[0]);
            if (optionalTarget.isEmpty()) {
                MessageUtils.sendI18NMessage(invocation.source(), "proxy.player_404");
                return;
            }
            Player target = optionalTarget.get();
            PlayerLoader loader = PlayerLoader.load(target);

            PreparedStatement ps = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM core_banlog WHERE userid=?");
            ps.setInt(1, loader.getId());
            ResultSet result = ps.executeQuery();

            List<HistoryEntry> targetHistory = new ArrayList<>();
            while (result.next()) {
                String reason = result.getString("reason");
                if (MathUtils.isInt(reason)) {
                    reason = PunishmentReason.loadReason(Integer.parseInt(reason)).getName();
                }

                String staff = result.getString("staff");
                if (MathUtils.isInt(staff)) {
                    PreparedStatement psStaff = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("SELECT * from core_playercache WHERE id=?");
                    psStaff.setInt(1, Integer.parseInt(staff));
                    ResultSet rsStaff = psStaff.executeQuery();
                    if (rsStaff.next())
                        staff = rsStaff.getString("lastname");
                    else
                        staff = "Console";
                    rsStaff.close();
                    psStaff.close();
                }

                StringBuilder bobTheActionBuilder = new StringBuilder();
                String dbAction = result.getString("action");
                switch (dbAction) {
                    case "kick":
                    case "warn":
                        bobTheActionBuilder.append("§e").append(dbAction);
                        break;
                    case "mute":
                        bobTheActionBuilder.append("§b").append(dbAction);
                        break;
                    case "ban":
                        bobTheActionBuilder.append("§c").append(dbAction);
                        break;
                    case "unban":
                        bobTheActionBuilder.append("§a").append(dbAction);
                        break;
                    default:
                        bobTheActionBuilder.append(dbAction);
                        break;
                }

                targetHistory.add(new HistoryEntry(
                        result.getInt("id"),
                        StringUtils.firstLetterUppercase(bobTheActionBuilder.toString()),
                        reason,
                        staff,
                        result.getTimestamp("date")
                ));
            }

            invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), "proxy.command.history.begin").replace("{player}", target.getUsername())));
            targetHistory.forEach(entry -> invocation.source().sendMessage(Component.text(MessageUtils.getI18NMessage(invocation.source(), "proxy.command.history.entry")
                    .replace("{id}", String.valueOf(entry.getId()))
                    .replace("{action}", entry.getAction())
                    .replace("{reason}", entry.getReason())
                    .replace("{staff}", entry.getStaff())
                    .replace("{date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.getDate())))));

            result.close();
            ps.close();

        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> ret = new ArrayList<>();

        if (invocation.arguments().length == 1) {
            ProxyProvider.getProxy().getServer().getAllPlayers().forEach(player -> ret.add(player.getUsername()));
        }

        return ret;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("proxy.command.history");
    }

    @AllArgsConstructor
    @Getter
    public static class HistoryEntry {
        private int id;
        private String action;
        private String reason;
        private String staff;
        private Date date;
    }

}
