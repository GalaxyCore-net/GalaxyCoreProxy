package net.galaxycore.galaxycoreproxy.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;
import net.galaxycore.galaxycoreproxy.configuration.internationalisation.I18N;
import net.galaxycore.galaxycoreproxy.utils.TimeDelay;
import net.kyori.adventure.text.Component;

public class BroadcastScheduler implements Runnable {

    private final GalaxyCoreProxy proxy;

    public BroadcastScheduler(GalaxyCoreProxy proxy) {
        this.proxy = proxy;
        TimeDelay delay = TimeDelay.readTimeDelay(proxy, "proxy.broadcast.delay");
        ScheduledTask scheduledTask = proxy.getServer().getScheduler().buildTask(proxy, this)
                .repeat(delay.getDelay(), delay.getDelayUnit()).delay(delay.getDelay(), delay.getDelayUnit()).schedule();
    }

    @Override
    public void run() {
        proxy.getServer().sendMessage(Component.text("\n" + I18N.getByLang("de_DE", "proxy.scheduler.broadcast") + "\n"));
    }

}
