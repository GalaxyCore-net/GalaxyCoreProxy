package net.galaxycore.galaxycoreproxy.maintenance

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.galaxycore.galaxycoreproxy.GalaxyCoreProxy
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider
import net.kyori.adventure.text.Component
import org.slf4j.Logger

class MaintenanceCommand(
    commandManager: CommandManager,
    logger: Logger,
    maintenance: Maintenance,
    proxy: GalaxyCoreProxy
) {
    private val player404 =
        "Player not found"
    private val usage = """
        |Usage:
        |/maintenance <command>
        |
        |Base:
        |/maintenance enable - Enables maintenance mode
        |/maintenance disable - Disables maintenance mode
        |/maintenance status - Shows maintenance status
        |/maintenance emergency - Enables emergency maintenance mode
        
        |List:
        |/maintenance list get - Lists all players in maintenance mode
        |/maintenance list add <p> - Adds a player to maintenance mode
        |/maintenance list remove <p> - Removes a player from maintenance mode
        
        |Beta:
        |/maintenance beta enable - Enables beta mode
        |/maintenance beta disable - Disables beta mode
        |/maintenance beta generate - Generates a new beta key
        |/maintenance beta invalidate <k> - Invalidates a beta key
        |/maintenance beta add <p> - Adds User to beta mode
        |/maintenance beta remove <p> - Removes User from beta mode
        |/maintenance beta list - Lists all users in beta mode //
    """.trimIndent()

    init {
        val commandNode: LiteralCommandNode<CommandSource> =
            LiteralArgumentBuilder.literal<CommandSource>("maintenance")
                .requires {
                    it.hasPermission(
                        "galaxycore.command.maintenance"
                    )
                }
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>(
                        "enable"
                    )
                        .executes {
                            maintenance.enable()
                            logger.info(
                                "Maintenance mode enabled"
                            )
                            it.source.sendMessage(
                                Component.text(
                                    "Maintenance mode enabled"
                                )
                            )
                            0
                        }
                ).then(
                    LiteralArgumentBuilder.literal<CommandSource>(
                        "status"
                    )
                        .executes {
                            it.source.sendMessage(
                                Component.text(
                                    "Status: ${ProxyProvider.proxy.proxyNamespace.get("maintenance").toBoolean()}"
                                )
                            )
                            0
                        }
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("disable")
                    .executes {
                        maintenance.disable()
                        logger.info(
                            "Maintenance mode disabled"
                        )
                        it.source.sendMessage(
                            Component.text(
                                "Maintenance mode disabled"
                            )
                        )
                        0
                    }
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("list")
                    .then(LiteralArgumentBuilder.literal<CommandSource>("get")
                        .executes {
                            it.source.sendMessage(
                                Component.text(
                                    "Maintenance mode players: "
                                )
                            )
                            for (player: Player in maintenance.players) {
                                it.source.sendMessage(
                                    Component.text(
                                        player.username
                                    )
                                )
                            }
                            0
                        }
                    )
                    .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                        .then(RequiredArgumentBuilder.argument<CommandSource, String>(
                            "player",
                            StringArgumentType.string()
                        )
                            .suggests { _, builder ->
                                ProxyProvider.proxy.server.allPlayers.map { it.username }
                                    .forEach { builder.suggest(it) }
                                builder.buildFuture()
                            }
                            .executes {
                                val player =
                                    proxy.server.getPlayer(
                                        getString(
                                            it,
                                            "player"
                                        )
                                    )
                                if (player.isPresent) {
                                    maintenance.addPlayer(
                                        player.get(),
                                        maintenance = true,
                                        beta = false,
                                        emergency = false
                                    )
                                    it.source.sendMessage(
                                        Component.text(
                                            "Maintenance mode player added: ${player.get().username}"
                                        )
                                    )
                                } else
                                    it.source.sendMessage(
                                        Component.text(
                                            player404
                                        )
                                    )

                                0
                            }
                        )
                    )
                    .then(LiteralArgumentBuilder.literal<CommandSource>("remove")
                        .then(RequiredArgumentBuilder.argument<CommandSource, String>(
                            "player",
                            StringArgumentType.string()
                        )
                            .suggests { _, builder ->
                                ProxyProvider.proxy.server.allPlayers.map { it.username }
                                    .forEach {
                                        builder.suggest(
                                            it
                                        )
                                    }
                                builder.buildFuture()
                            }
                            .executes {
                                val player =
                                    proxy.server.getPlayer(
                                        getString(
                                            it,
                                            "player"
                                        )
                                    )
                                if (player.isPresent) {
                                    maintenance.removePlayer(
                                        player.get()
                                    )
                                    it.source.sendMessage(
                                        Component.text(
                                            "Maintenance mode player removed: ${player.get().username}"
                                        )
                                    )
                                } else
                                    it.source.sendMessage(
                                        Component.text(
                                            player404
                                        )
                                    )
                                0
                            }
                        )
                    )
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("beta")
                    .then(LiteralArgumentBuilder.literal<CommandSource>("enable")
                        .executes {
                            maintenance.enableBeta()
                            logger.info(
                                "Beta mode enabled"
                            )
                            it.source.sendMessage(
                                Component.text(
                                    "Beta mode enabled"
                                )
                            )
                            0
                        }
                    )
                    .then(LiteralArgumentBuilder.literal<CommandSource>("disable")
                        .executes {
                            maintenance.disableBeta()
                            logger.info(
                                "Beta mode disabled"
                            )
                            it.source.sendMessage(
                                Component.text(
                                    "Beta mode disabled"
                                )
                            )
                            0
                        }
                    )
                    .then(LiteralArgumentBuilder.literal<CommandSource>("generate")
                        .executes {
                            it.source.sendMessage(
                                Component.text(
                                    "Your beta Key: ${maintenance.generateKey()}"
                                )
                            )
                            0
                        }
                    )
                    .then(LiteralArgumentBuilder.literal<CommandSource>("invalidate")
                        .then(RequiredArgumentBuilder.argument<CommandSource, String>(
                            "key",
                            StringArgumentType.string()
                        )
                            .executes {
                                maintenance.invalidateKey(
                                    getString(
                                        it,
                                        "key"
                                    )
                                )
                                it.source.sendMessage(
                                    Component.text(
                                        "Beta mode key invalidated"
                                    )
                                )
                                0
                            }
                        )
                    )
                    // add a player to the allowed beta players
                    .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, String>(
                                "player",
                                StringArgumentType.string()
                            )
                                .suggests { _, builder ->
                                    ProxyProvider.proxy.server.allPlayers.map { it.username }
                                        .forEach {
                                            builder.suggest(
                                                it
                                            )
                                        }
                                    builder.buildFuture()
                                }
                                .executes {
                                    val player =
                                        proxy.server.getPlayer(
                                            getString(
                                                it,
                                                "player"
                                            )
                                        )
                                    if (player.isPresent) {
                                        maintenance.addPlayer(
                                            player.get(),
                                            maintenance = false,
                                            beta = true,
                                            emergency = false
                                        )
                                        it.source.sendMessage(
                                            Component.text(
                                                "Beta mode player added: ${player.get().username}"
                                            )
                                        )
                                    } else
                                        it.source.sendMessage(
                                            Component.text(
                                                player404
                                            )
                                        )
                                    0
                                }
                        )
                    )
                    // add a player to the allowed beta players
                    .then(LiteralArgumentBuilder.literal<CommandSource>("remove")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, String>(
                                "player",
                                StringArgumentType.string()
                            )
                                .suggests { _, builder ->
                                    ProxyProvider.proxy.server.allPlayers.map { it.username }
                                        .forEach {
                                            builder.suggest(
                                                it
                                            )
                                        }
                                    builder.buildFuture()
                                }
                                .executes {
                                    val player =
                                        proxy.server.getPlayer(
                                            getString(
                                                it,
                                                "player"
                                            )
                                        )
                                    if (player.isPresent) {
                                        maintenance.removePlayer(
                                            player.get()
                                        )
                                        it.source.sendMessage(
                                            Component.text(
                                                "Beta mode player removed: ${player.get().username}"
                                            )
                                        )
                                    } else
                                        it.source.sendMessage(
                                            Component.text(
                                                player404
                                            )
                                        )
                                    0
                                }
                        )
                    )
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("emergency")
                    .executes {
                        maintenance.toggleEmergency()
                        it.source.sendMessage(
                            Component.text(
                                "Emergency mode activated"
                            )
                        )

                        0
                    }
                )
                .executes {
                    it.source.sendMessage(Component.text(usage))
                    0
                }
                .build()
        val brigadierCommand = BrigadierCommand(commandNode)
        commandManager.register(brigadierCommand)
    }

}
