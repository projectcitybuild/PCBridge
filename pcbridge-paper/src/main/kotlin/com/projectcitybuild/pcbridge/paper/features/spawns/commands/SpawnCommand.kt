package com.projectcitybuild.pcbridge.paper.features.spawns.commands

import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class SpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
    private val playerTeleporter: PlayerTeleporter,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("spawn")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()

        val spawn = spawnRepository.get(player.location.world)

        playerTeleporter.move(
            player = player,
            destination = spawn,
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendRichMessage(l10n.teleportedToSpawn)
    }
}