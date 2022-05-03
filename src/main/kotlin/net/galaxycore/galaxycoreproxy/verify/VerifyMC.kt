package net.galaxycore.galaxycoreproxy.verify

import com.velocitypowered.api.command.CommandManager
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VerifyMC(
    commandManager: CommandManager,
    proxy: GalaxyCoreProxy
) {
    private val verifyCommand: VerifyCommand
    private val logger: Logger =
        LoggerFactory.getLogger(
            VerifyMC::class.java
        )

    init {
        logger.info(
            "Initializing verify subsystem"
        )
        verifyCommand =
            VerifyCommand(
                commandManager,
                proxy
            )
        logger.info(
            "Verify subsystem initialized"
        )
    }
}