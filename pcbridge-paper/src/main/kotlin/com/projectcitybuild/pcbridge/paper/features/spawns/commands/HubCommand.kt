package com.projectcitybuild.pcbridge.paper.features.spawns.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

class HubCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val remoteConfig: RemoteConfig,
    private val playerTeleporter: PlayerTeleporter,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("hub")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.requirePlayer()

        val hub = remoteConfig.latest.config.hub
            ?: throw Exception("Hub world not set")

        val worldId = UUID.fromString(hub.worldId)
        val world = server.getWorld(worldId)
        if (world == null) {
            player.sendRichMessage("<red>Error: Could not find hub world</red>")
            return@traceSuspending
        }
        val location = Location(world, hub.x, hub.y, hub.z, hub.yaw, hub.pitch)

        playerTeleporter.move(
            player = player,
            destination = location,
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendRichMessage("<green>⚡ Teleported to hub</green>")
    }
}