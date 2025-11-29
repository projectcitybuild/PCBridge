package com.projectcitybuild.pcbridge.paper.features.moderate.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.broadcastRich
import com.projectcitybuild.pcbridge.paper.features.moderate.moderateTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.java

class KickCommand(
    private val plugin: JavaPlugin,
    private val server: Server,
): BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("kick")
            .requiresPermission(PermissionNode.MODERATE)
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .then(
                        Commands.argument("reason", StringArgumentType.greedyString())
                            .executesSuspending(plugin, ::execute)
                    )
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(moderateTracer) {
        val player = context.getArgument("player", Player::class.java)
        val reason = context.getOptionalArgument("reason", String::class.java)

        val message = MiniMessage.miniMessage().deserialize(
            reason ?: l10n.kickedFromServer,
        )
        player.kick(message, PlayerKickEvent.Cause.KICK_COMMAND)

        server.broadcastRich(
            if (!reason.isNullOrEmpty())
                l10n.playerWasKickedForReason(player.name, reason)
            else
                l10n.playerWasKicked(player.name)
        )
    }
}