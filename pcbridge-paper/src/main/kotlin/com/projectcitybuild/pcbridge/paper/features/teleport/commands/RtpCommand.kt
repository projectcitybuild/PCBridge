package com.projectcitybuild.pcbridge.paper.features.teleport.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.RandomLocationFinder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class RtpCommand(
    private val plugin: Plugin,
    private val randomLocationFinder: RandomLocationFinder,
    private val playerTeleporter: PlayerTeleporter,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("rtp")
            .requiresPermission(PermissionNode.TELEPORT_RANDOM)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val executor = context.source.executor
        val player = executor as? Player
        checkNotNull(player) { "Only players can use this command" }

        executor.sendMessage(
            miniMessage.deserialize("<gray><italic>Teleporting...</italic></gray>")
        )

        val world = player.location.world
        val destination = randomLocationFinder.find(world, attempts = 5)
        if (destination == null) {
            executor.sendMessage(
                miniMessage.deserialize("<red>Failed to find a safe location</red>")
            )
            return@traceSuspending
        }
        playerTeleporter.move(
            player,
            destination,
            options = PlayerTeleporter.TeleportOptions(
                cause = PlayerTeleportEvent.TeleportCause.COMMAND,
                preloadDestinationChunks = true,
                snapToBlockCenter = true,
            ),
        )
        executor.sendMessage(
            miniMessage.deserialize("<green>Teleported to </green><gray>x=${destination.x}, y=${destination.y}, z=${destination.z}</gray>")
        )
    }
}