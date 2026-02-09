package com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
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
    override fun literal(): PaperCommandNode {
        return Commands.literal("hub")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: PaperCommandContext) = context.scoped(spawnsTracer) {
        val player = context.source.requirePlayer()

        val hub = remoteConfig.latest.config.hub
        if (hub == null) {
            log.error { "Hub not set" }
            player.sendRichMessage(l10n.errorHubNotSet)
            return@scoped
        }

        val worldId = UUID.fromString(hub.worldId)
        val world = server.getWorld(worldId)
        if (world == null) {
            log.error { "Hub world not found for id ${hub.worldId}" }
            player.sendRichMessage(l10n.errorHubWorldNotFound)
            return@scoped
        }
        val location = Location(world, hub.x, hub.y, hub.z, hub.yaw, hub.pitch)

        playerTeleporter.move(
            player = player,
            destination = location,
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendRichMessage(l10n.teleportedToHub)
    }
}