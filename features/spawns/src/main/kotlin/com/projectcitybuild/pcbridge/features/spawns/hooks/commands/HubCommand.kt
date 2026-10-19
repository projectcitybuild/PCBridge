package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
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
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("hub")
        .executesSuspending(commandTaskExecutor, ::execute)
        .build()

    suspend fun execute(context: PaperCommandContext) = context.scoped(spawnsTracer) {
        val player = context.source.requirePlayer()

        val hub = remoteConfig.latest.config.hub
        if (hub == null) {
            log.error { "Hub not set" }
            player.sendMessage(l10n.spawns.errorHubNotSet)
            return@scoped
        }

        val worldId = UUID.fromString(hub.worldId)
        val world = server.getWorld(worldId)
        if (world == null) {
            log.error { "Hub world (${hub.worldId}) not found" }
            player.sendMessage(l10n.spawns.errorHubWorldNotFound)
            return@scoped
        }
        val location = Location(world, hub.x, hub.y, hub.z, hub.yaw, hub.pitch)

        playerTeleporter.move(
            player = player,
            destination = location,
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendMessage(l10n.spawns.teleportedToHub)
    }
}
