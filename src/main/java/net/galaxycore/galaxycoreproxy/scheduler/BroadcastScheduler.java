package net.galaxycore.galaxycoreproxy.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.SneakyThrows;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.utils.SQLUtils;
import net.galaxycore.galaxycoreproxy.utils.TimeDelay;
import net.kyori.adventure.text.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BroadcastScheduler implements Runnable {

    private final GalaxyCoreProxy proxy;

    @Getter
    private final List<ScheduledTask> scheduledMessages = new ArrayList<>();

    @SneakyThrows
    public BroadcastScheduler(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        SQLUtils.runScript(proxy.getDatabaseConfiguration(), "broadcast", "initialize");

        PreparedStatement preparedStatement = proxy.getDatabaseConfiguration().getConnection().prepareStatement("SELECT * FROM proxy_broadcasts");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            TimeDelay delay = TimeDelay.readTimeDelay(resultSet.getString("delay"));
            String i18nKey = resultSet.getString("message");
            ScheduledTask scheduledTask = proxy.getServer().getScheduler().buildTask(proxy, () ->
                    proxy.getServer().sendMessage(Component.text("\n" + I18N.getByLang("de_DE", i18nKey))))
                    .delay(delay.getDelay(), delay.getDelayUnit()).repeat(delay.getDelay(), delay.getDelayUnit()).schedule();
            scheduledMessages.add(scheduledTask);
        }
        resultSet.close();
        preparedStatement.close();
    }

    @Override
    public void run() {
        proxy.getServer().sendMessage(Component.text("\n" + I18N.getByLang("de_DE", "proxy.scheduler.broadcast") + "\n"));
    }

}
