package net.galaxycore.galaxycoreproxy.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@AllArgsConstructor
public class TimeDelay {

    private int delay;
    private TimeUnit delayUnit;

    public static TimeDelay readTimeDelay(GalaxyCoreProxy proxy, String path) {
        TimeDelay delay = new TimeDelay(1, TimeUnit.HOURS);
        String configDelay = proxy.getProxyNamespace().get(path);
        try {
            delay.setDelay(Integer.parseInt(configDelay.substring(0, configDelay.length() - 1)));
        }catch (Exception ignore) {}
        switch (configDelay.substring(configDelay.length() - 1)) {
            case "s":
                delay.setDelayUnit(TimeUnit.SECONDS);
                break;
            case "m":
                delay.setDelayUnit(TimeUnit.MINUTES);
                break;
            case "h":
                delay.setDelayUnit(TimeUnit.HOURS);
                break;
            case "d":
                delay.setDelayUnit(TimeUnit.DAYS);
                break;
            default:
                break;
        }
        return delay;
    }

}
