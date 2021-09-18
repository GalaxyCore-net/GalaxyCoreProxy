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

    public static TimeDelay readTimeDelay(String delay) {
        TimeDelay timeDelay = new TimeDelay(1, TimeUnit.HOURS);
        try {
            timeDelay.setDelay(Integer.parseInt(delay.substring(0, delay.length() - 1)));
        }catch (Exception ignore) {}
        switch (delay.substring(delay.length() - 1)) {
            case "s":
                timeDelay.setDelayUnit(TimeUnit.SECONDS);
                break;
            case "m":
                timeDelay.setDelayUnit(TimeUnit.MINUTES);
                break;
            case "h":
                timeDelay.setDelayUnit(TimeUnit.HOURS);
                break;
            case "d":
                timeDelay.setDelayUnit(TimeUnit.DAYS);
                break;
            default:
                break;
        }
        return timeDelay;
    }

}
