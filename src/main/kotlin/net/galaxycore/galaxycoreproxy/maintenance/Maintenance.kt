package net.galaxycore.galaxycoreproxy.maintenance

import com.velocitypowered.api.command.CommandManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Maintenance(commandManager: CommandManager) {
    private val maintenanceCommand: MaintenanceCommand
    private val logger: Logger = LoggerFactory.getLogger(Maintenance::class.java)

    init {
        logger.info("Initializing maintenance subsystem")
        maintenanceCommand = MaintenanceCommand(commandManager, logger)


        logger.info("Maintenance subsystem initialized")
    }
}