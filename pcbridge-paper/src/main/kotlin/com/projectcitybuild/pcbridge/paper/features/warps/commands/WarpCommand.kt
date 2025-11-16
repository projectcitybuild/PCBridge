package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.pcb.models.Warp
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class WarpCommand(
    private val plugin: Plugin,
    private val warpNameSuggester: WarpNameSuggester,
    private val warpRepository: WarpRepository,
    private val server: Server,
    private val playerTeleporter: PlayerTeleporter,
) : BrigadierCommand {
    override val description: String = "Teleports to a warp"

    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("warp")
            .requiresPermission(PermissionNode.WARP_TELEPORT)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggestsSuspending(plugin, warpNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()
        val warpName = context.getArgument("name", String::class.java)

        val warp = warpRepository.get(name = warpName)
        checkNotNull(warp) { l10n.errorWarpNotFound(warpName) }

        val world = server.getWorld(warp.world)
        checkNotNull(world) { l10n.errorWorldNotFound(warp.world) }

        playerTeleporter.move(
            player,
            destination = warp.toLocation(world),
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendRichMessage(l10n.teleportedToName(warp.name))
    }
}

private fun Warp.toLocation(world: World)
    = Location(world, x, y, z, yaw, pitch,)