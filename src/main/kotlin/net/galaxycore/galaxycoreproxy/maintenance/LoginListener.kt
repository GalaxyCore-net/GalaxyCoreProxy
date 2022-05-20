package net.galaxycore.galaxycoreproxy.maintenance

import com.velocitypowered.api.event.ResultedEvent.ComponentResult
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider
import net.kyori.adventure.text.Component

class LoginListener {

    init {
        ProxyProvider.proxy.registerListener(this)
    }

    @Subscribe
    fun onLogin(loginEvent: LoginEvent) {

        val maintenance = ProxyProvider.proxy.proxyNamespace.get("maintenance").toBoolean()
        val beta = ProxyProvider.proxy.proxyNamespace.get("maintenance_beta").toBoolean()
        val emergency = ProxyProvider.proxy.proxyNamespace.get("maintenance_emergency").toBoolean()
        if (maintenance || beta || emergency) {
            val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("SELECT * FROM `maintenance_players` WHERE uuid=?")
            stmt.setString(1, loginEvent.player.uniqueId.toString())
            val rs = stmt.executeQuery()
            if (rs.next()) {
                if (maintenance && !rs.getBoolean("maintenance")) {
                    loginEvent.result = denyMaintenance()
                } else if (beta && !rs.getBoolean("beta")) {
                    loginEvent.result = denyBeta()
                } else if (emergency && !rs.getBoolean("emergency")) {
                    loginEvent.result = denyEmergency()
                }
            } else {
                if (maintenance) {
                    if (!loginEvent.player.hasPermission("galaxycore.maintenance.bypass")) {
                        loginEvent.result = denyMaintenance()
                    }
                } else if (beta) {
                    if (!loginEvent.player.hasPermission("galaxycore.maintenance.bypass") && !loginEvent.player.hasPermission("galaxycore.beta.bypass")) {
                        loginEvent.result = denyBeta()
                    }
                } else {
                    if (!loginEvent.player.hasPermission("galaxycore.emergency.bypass")) {
                        loginEvent.result = denyEmergency()
                    }
                }
            }
        }

    }

    private fun denyMaintenance(): ComponentResult = ComponentResult.denied(Component.text("Maintenance Mode enabled. You can't join unless you are allowed to"))

    private fun denyBeta(): ComponentResult = ComponentResult.denied(Component.text("Beta Mode enabled. You can't join unless you are allowed to"))

    private fun denyEmergency(): ComponentResult = ComponentResult.denied(Component.text("Emergency Mode enabled. You can't join unless you are allowed to"))

}