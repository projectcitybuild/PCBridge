package com.projectcitybuild.pcbridge.features.moderate.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.moderate.ModeratePermissions
import com.projectcitybuild.pcbridge.features.moderate.moderateTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.java

class KickCommand(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("kick") {
            description = "Kicks a player from the server"
            requiresPermission(ModeratePermissions.MODERATE)
            argument("player", SingleOnlinePlayerArgument(plugin.server)) {
                then(
                    Commands
                        .argument("reason", StringArgumentType.greedyString())
                        .executesSuspending(taskExecutor, ::execute),
                ).executesSuspending(taskExecutor, ::execute)
            }
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(moderateTracer) {
        val player = context.getArgument("player", Player::class.java)
        val reason = context.getOptionalArgument("reason", String::class.java)

        val message = reason?.let { Component.text(it) }
            ?: l10n.moderation.kickedFromServer

        player.kick(message, PlayerKickEvent.Cause.KICK_COMMAND)

        server.broadcast(
            if (!reason.isNullOrEmpty()) {
                l10n.moderation.playerWasKickedForReason(player.name, reason)
            } else {
                l10n.moderation.playerWasKicked(player.name)
            },
        )
    }
}
