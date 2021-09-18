package net.galaxycore.galaxycoreproxy.configuration;

import lombok.Getter;
import lombok.Setter;
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy;

public class ProxyProvider {
    @Getter
    @Setter
    private static GalaxyCoreProxy proxy;
}
