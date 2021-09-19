package net.galaxycore.galaxycoreproxy.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.MessageUtils;
import net.galaxycore.galaxycoreproxy.utils.SQLUtils;
import net.galaxycore.galaxycoreproxy.utils.TimeDelay;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BroadcastScheduler {

    @Getter
    private final List<ScheduledTask> scheduledMessages = new ArrayList<>();

    @SneakyThrows
    public BroadcastScheduler() {
        SQLUtils.runScript(ProxyProvider.getProxy().getDatabaseConfiguration(), "broadcast", "initialize");

        PreparedStatement preparedStatement = ProxyProvider.getProxy().getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM proxy_broadcasts");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            TimeDelay delay = TimeDelay.readTimeDelay(resultSet.getString("delay"));
            String i18nKey = resultSet.getString("message");
            ScheduledTask scheduledTask = ProxyProvider.getProxy().getServer().getScheduler().buildTask(ProxyProvider.getProxy(), () ->
                            MessageUtils.broadcastI18NMessage(i18nKey))
                    .delay(delay.getDelay(), delay.getDelayUnit()).repeat(delay.getDelay(), delay.getDelayUnit()).schedule();
            scheduledMessages.add(scheduledTask);
        }
        resultSet.close();
        preparedStatement.close();
    }

}
