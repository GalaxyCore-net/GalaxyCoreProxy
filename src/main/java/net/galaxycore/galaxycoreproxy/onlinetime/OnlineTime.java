package net.galaxycore.galaxycoreproxy.onlinetime;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider;
import net.galaxycore.galaxycoreproxy.utils.SQLUtils;

import java.util.concurrent.TimeUnit;

@Getter
public class OnlineTime {
    private final ProxyServer proxyServer;

    public static final long hashCodeSecret = 7472348L;
    private final OnlineTimeInterpolator interpolator;
    private final OnlineTimeSave save;
    private final ScheduledTask scheduler;

    public OnlineTime(ProxyServer proxyServer) {
        SQLUtils.runScript(ProxyProvider.getProxy().getDatabaseConfiguration(), "onlinetime", "initialize");

        this.proxyServer = proxyServer;
        this.save = new OnlineTimeSave(this);
        this.interpolator = new OnlineTimeInterpolator(this);
        this.scheduler = this.proxyServer.getScheduler().buildTask(ProxyProvider.getProxy(), this.save).repeat(2, TimeUnit.MINUTES).schedule();
    }
}
