package net.galaxycore.galaxycoreproxy.maintenance

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandSource
import net.galaxycore.galaxycoreproxy.configuration.ProxyProvider
import net.kyori.adventure.text.Component
import org.slf4j.Logger

class MaintenanceCommand(commandManager: CommandManager, logger: Logger) {
    init {
        val commandNode: LiteralCommandNode<CommandSource> = LiteralArgumentBuilder.literal<CommandSource>("maintenance")
            .requires { it.hasPermission("galaxycore.command.maintenance") }
            .then(LiteralArgumentBuilder.literal<CommandSource>("enable")
                .executes {
                    it.source.sendMessage(Component.text("Maintenance mode enabled"))
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("disable")
                .executes {
                    it.source.sendMessage(Component.text("Maintenance mode disabled"))
                    0
                }
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("list")
                .then(LiteralArgumentBuilder.literal<CommandSource>("get")
                    .executes {
                        it.source.sendMessage(Component.text("Maintenance mode players: "))
                        0
                    }
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                        .suggests {_, builder ->
                            ProxyProvider.proxy.server.allPlayers.map { it.username }.forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes {
                            it.source.sendMessage(Component.text("Maintenance mode player added: "))
                            0
                        }
                    )
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("remove")
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                        .suggests { _, builder ->
                            builder.suggest("player")
                            builder.buildFuture()
                        }
                        .executes {
                            it.source.sendMessage(Component.text("Maintenance mode player removed: "))
                            0
                        }
                    )
                )
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("beta")
                .then(LiteralArgumentBuilder.literal<CommandSource>("enable")
                    .executes {
                        it.source.sendMessage(Component.text("Beta mode enabled"))
                        0
                    }
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("disable")
                    .executes {
                        it.source.sendMessage(Component.text("Beta mode disabled"))
                        0
                    }
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("generate")
                    .executes {
                        it.source.sendMessage(Component.text("Beta mode key generated"))
                        0
                    }
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("invalidate")
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("key", StringArgumentType.string())
                        .executes {
                            it.source.sendMessage(Component.text("Beta mode key invalidated"))
                            0
                        }
                    )
                )
                // add a player to the allowed beta players
                .then(LiteralArgumentBuilder.literal<CommandSource>("add")
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                        .suggests { ctx, builder ->
                            ProxyProvider.proxy.server.allPlayers.map { it.username }.forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes {
                            it.source.sendMessage(Component.text("Beta mode player added: "))
                            0
                        }
                    )
                )
            )
            .then(LiteralArgumentBuilder.literal<CommandSource>("emergency")
                .executes {
                    it.source.sendMessage(Component.text("Emergency mode activated"))

                    0
                }
            )
            .build()
        val brigadierCommand = BrigadierCommand(commandNode)
        commandManager.register(brigadierCommand)
    }

}
