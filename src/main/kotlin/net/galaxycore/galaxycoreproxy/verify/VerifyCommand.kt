package net.galaxycore.galaxycoreproxy.verify

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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger

class VerifyCommand(
    commandManager: CommandManager,
    proxy: GalaxyCoreProxy
) {
    init {
        // /verify <code>
        val commandNode: LiteralCommandNode<CommandSource> =
            LiteralArgumentBuilder.literal<CommandSource>(
                "verify"
            )
                .requires {
                    it.hasPermission(
                        "galaxycore.command.verify"
                    )
                }
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, String>(
                        "code",
                        StringArgumentType.string()
                    )
                        .executes {
                            val code =
                                getString(
                                    it,
                                    "code"
                                )

                            post(
                                proxy.proxyNamespace.get(
                                    "api_base_url"
                                ) + "/verify?code=$code&uuid=${
                                    Player::class.java.cast(
                                        it.source
                                    ).uniqueId
                                }"
                            )

                            0
                        })
                .build()
        val brigadierCommand =
            BrigadierCommand(
                commandNode
            )
        commandManager.register(
            brigadierCommand
        )
    }

    private val jsonMediaType: MediaType =
        "application/json; charset=utf-8".toMediaType()

    private fun post(
        url: String
    ): String? {
        val client =
            OkHttpClient()
        val request: Request =
            Request.Builder()
                .url(
                    url
                )
                .method(
                    "POST",
                    "".toRequestBody(
                        jsonMediaType
                    )
                )
                .build()
        val res =
            client.newCall(
                request
            )
                .execute()
        return res.body?.string()
    }
}
