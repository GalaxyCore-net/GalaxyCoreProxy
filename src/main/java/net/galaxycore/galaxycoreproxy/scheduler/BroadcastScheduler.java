package net.galaxycore.galaxycoreproxy.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public class BroadcastScheduler implements Runnable {

    private final GalaxyCoreProxy proxy;

    public BroadcastScheduler(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        int period = 1;
        TimeUnit periodUnit = TimeUnit.MINUTES;
        String proxyBroadcastDelay = proxy.getProxyNamespace().get("proxy.broadcast.delay");
        try {
            period = Integer.parseInt(proxyBroadcastDelay.substring(0, proxyBroadcastDelay.length() - 1));
        }catch (Exception ignore) {}
        switch (proxyBroadcastDelay.substring(proxyBroadcastDelay.length() - 1)) {
            case "s":
                periodUnit = TimeUnit.SECONDS;
                break;
            case "h":
                periodUnit = TimeUnit.HOURS;
                break;
            case "d":
                periodUnit = TimeUnit.DAYS;
                break;
            default:
                break;
        }
        ScheduledTask scheduledTask = proxy.getServer().getScheduler().buildTask(proxy, this).repeat(period, periodUnit).delay(period, periodUnit).schedule();
        proxy.getLogger().info(scheduledTask.status().name());
    }

    @Override
    public void run() {
        proxy.getServer().sendMessage(Component.text("\n" + I18N.getByLang("de_DE", "proxy.scheduler.broadcast") + "\n"));
        proxy.getLogger().info("Sended Message");
    }

}
