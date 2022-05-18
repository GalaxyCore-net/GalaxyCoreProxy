package net.galaxycore.galaxycoreproxy.maintenance

import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.proxy.Player
import de.dytanic.cloudnet.driver.CloudNetDriver
import de.dytanic.cloudnet.ext.syncproxy.AbstractSyncProxyManagement
import de.dytanic.cloudnet.ext.syncproxy.configuration.SyncProxyConfiguration
import de.dytanic.cloudnet.ext.syncproxy.configuration.SyncProxyMotd
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider
import net.galaxycore.galaxycoreproxy.utils.SQLUtils
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import kotlin.random.Random

class Maintenance(commandManager: CommandManager) {
    private val maintenanceCommand: MaintenanceCommand
    private val loginListener: LoginListener
    private val logger: Logger = LoggerFactory.getLogger(Maintenance::class.java)
    var players = mutableListOf<Player>()
    private val syncProxyManagement = CloudNetDriver.getInstance().servicesRegistry.getFirstService(AbstractSyncProxyManagement::class.java)

    private val host = ProxyProvider.proxy.databaseConfiguration.internalConfiguration.yamlConfig.getString("betakeys.host")
    private val port = ProxyProvider.proxy.databaseConfiguration.internalConfiguration.yamlConfig.getInt("betakeys.port")
    private val database = ProxyProvider.proxy.databaseConfiguration.internalConfiguration.yamlConfig.getString("betakeys.database")
    private val user = ProxyProvider.proxy.databaseConfiguration.internalConfiguration.yamlConfig.getString("betakeys.user")
    private val password = ProxyProvider.proxy.databaseConfiguration.internalConfiguration.yamlConfig.getString("betakeys.password")
    private val connection: Connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$database?autoReconnect=true", user, password)

    init {
        logger.info("Initializing maintenance subsystem")
        SQLUtils.runScript(ProxyProvider.proxy.databaseConfiguration, "maintenance", "initialize")
        maintenanceCommand =
            MaintenanceCommand(
                    commandManager,
                    logger,
                    this,
                    ProxyProvider.proxy
            )

        loginListener = LoginListener()

        val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("SELECT * FROM `maintenance_players`")
        val rs = stmt.executeQuery()
        while (rs.next()) {
            ProxyProvider.proxy.server.getPlayer(UUID.fromString(rs.getString("uuid"))).ifPresent { players.add(it) }
        }
        rs.close()
        stmt.close()

        if (connection.isClosed.not()) {
            logger.warn("Connection to database is closed")
        }

        logger.info("Maintenance subsystem initialized")
    }

    fun enable() {
        ProxyProvider.proxy.proxyNamespace.set("maintenance", "true")
        changeMaintenance(true)
        setMotd("maintenance")
    }

    fun disable() {
        ProxyProvider.proxy.proxyNamespace.set("maintenance", "false")
        changeMaintenance(false)
        setMotd("normal")
    }

    fun addPlayer(player: Player, maintenance: Boolean, beta: Boolean, emergency: Boolean) {
        players.add(player)
        val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("INSERT INTO `maintenance_players` (uuid, maintenance, beta, emergency) VALUES (?, ?, ?, ?)")
        stmt.setString(1, player.uniqueId.toString())
        stmt.setBoolean(2, maintenance)
        stmt.setBoolean(3, beta)
        stmt.setBoolean(4, emergency)
        stmt.executeUpdate()
        stmt.close()
    }

    fun removePlayer(player: Player) {
        players.remove(player)
        val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("DELETE FROM `maintenance_players` WHERE uuid=?")
        stmt.setString(1, player.uniqueId.toString())
        stmt.executeUpdate()
        stmt.close()
    }

    fun enableBeta() {
        ProxyProvider.proxy.proxyNamespace.set("maintenance_beta", "true")
        changeMaintenance(false)

        val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("SELECT * FROM `maintenance_players` WHERE beta=0")
        val rs = stmt.executeQuery()
        while (rs.next()) {
            ProxyProvider.proxy.server.getPlayer(UUID.fromString(rs.getString("uuid"))).ifPresent { it.disconnect(Component.text("Beta mode was activated. You can't join unless you have beta access!")) }
        }
        rs.close()
        stmt.close()
        setMotd("beta")
    }

    fun disableBeta() {
        ProxyProvider.proxy.proxyNamespace.set("maintenance_beta", "false")
        changeMaintenance(ProxyProvider.proxy.proxyNamespace.get("maintenance").toBoolean())
        setMotd("normal")
    }

    fun generateKey(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val betaKey = (1..12)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        logger.info("Beta key generated: $betaKey")

        val stmt = connection.prepareStatement("INSERT INTO `unused_keys` (key_name) VALUES (?)")
        stmt.setString(1, betaKey)
        stmt.executeUpdate()
        stmt.close()
        return betaKey
    }

    fun invalidateKey(key: String) {
        val stmt = connection.prepareStatement("DELETE FROM `unused_keys` WHERE key_name=?")
        stmt.setString(1, key)
        stmt.executeUpdate()
        stmt.close()
    }

    private fun changeMaintenance(maintenance: Boolean) {
        if (this.syncProxyManagement == null)
            return
        val loginConfiguration = this.syncProxyManagement.loginConfiguration

        if (loginConfiguration != null) {

            val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("SELECT * FROM `maintenance_players` WHERE maintenance=1")
            val rs = stmt.executeQuery()
            while (rs.next()) {
                ProxyProvider.proxy.server.getPlayer(UUID.fromString(rs.getString("uuid"))).ifPresent { loginConfiguration.whitelist.add(it.username) }
            }
            rs.close()
            stmt.close()

            loginConfiguration.isMaintenance = maintenance

            SyncProxyConfiguration.updateSyncProxyConfigurationInNetwork(this.syncProxyManagement.syncProxyConfiguration)
        }
    }

    private fun setMotd(scope: String) {
        if (this.syncProxyManagement == null)
            return
        val loginConfiguration = this.syncProxyManagement.loginConfiguration

        if (loginConfiguration != null) {
            loginConfiguration.motds = Collections.singletonList(
                    SyncProxyMotd(
                            ProxyProvider.proxy.proxyNamespace.get(scope + "_motd_1"),
                            ProxyProvider.proxy.proxyNamespace.get(scope + "_motd_2"),
                            false,
                            1,
                            mutableListOf<String>().toTypedArray(),
                            ProxyProvider.proxy.proxyNamespace.get(scope + "_motd_header").ifEmpty { null }
                    )
            )

            SyncProxyConfiguration.updateSyncProxyConfigurationInNetwork(this.syncProxyManagement.syncProxyConfiguration)
        }
    }

    fun toggleEmergency() {
        if (ProxyProvider.proxy.proxyNamespace.get("maintenance_emergency").toBoolean()) disableEmergency() else enableEmergency()
    }

    private fun enableEmergency() {
        ProxyProvider.proxy.proxyNamespace.set("maintenance_emergency", "true")
        changeMaintenance(true)

        val stmt = ProxyProvider.proxy.databaseConfiguration.connection.prepareStatement("SELECT * FROM `maintenance_players` WHERE emergency=0")
        val rs = stmt.executeQuery()
        while (rs.next()) {
            ProxyProvider.proxy.server.getPlayer(UUID.fromString(rs.getString("uuid"))).ifPresent { it.disconnect(Component.text("Emergency mode was activated. You can't join unless you have emergency access!")) }
        }
        rs.close()
        stmt.close()
        setMotd("emergency")
    }

    private fun disableEmergency() {
        ProxyProvider.proxy.proxyNamespace.set("maintenance_emergency", "false")
        changeMaintenance(ProxyProvider.proxy.proxyNamespace.get("maintenance").toBoolean())
        setMotd("normal")
    }
}