package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class SpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
    private val playerTeleporter: PlayerTeleporter,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("spawn") {
        description = "Teleports to the server spawn"
        executesSuspending(commandTaskExecutor, ::execute)
    }

    suspend fun execute(context: PaperCommandContext) = context.scoped(spawnsTracer) {
        val player = context.source.requirePlayer()

        val spawn = spawnRepository.get(player.location.world)

        playerTeleporter.move(
            player = player,
            destination = spawn,
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendMessage(l10n.spawns.teleportedToSpawn)
    }
}
