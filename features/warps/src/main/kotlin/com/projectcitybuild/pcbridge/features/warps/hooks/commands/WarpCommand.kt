package com.projectcitybuild.pcbridge.features.warps.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Warp
import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
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
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {

    override val commandDefinition by lazy {
        command("warp") {
            description = "Teleports to a warp"
            requiresPermission(WarpsPermissions.TELEPORT)
            argument("name", StringArgumentType.string()) {
                suggestsSuspending(plugin, warpNameSuggester::suggest)
                    .executesSuspending(taskExecutor, ::execute)
            }
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val player = context.source.requirePlayer()
        val warpName = context.getArgument("name", String::class.java)

        val warp = warpRepository.get(name = warpName)
        checkNotNull(warp) { l10n.warps.errorWarpNotFound(warpName) }

        val world = server.getWorld(warp.world)
        checkNotNull(world) { l10n.errors.errorWorldNotFound(warp.world) }

        playerTeleporter.move(
            player,
            destination = warp.toLocation(world),
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        player.sendMessage(l10n.teleportation.teleportedToName(warp.name))
    }
}

private fun Warp.toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
