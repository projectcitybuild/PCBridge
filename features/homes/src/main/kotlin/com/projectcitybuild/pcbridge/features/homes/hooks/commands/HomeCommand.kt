package com.projectcitybuild.pcbridge.features.homes.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Home
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.homesTracer
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class HomeCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val homeNameSuggester: HomeNameSuggester,
    private val homeRepository: HomeRepository,
    private val playerTeleporter: PlayerTeleporter,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("home")
            .requiresPermission(PermissionNode.HOMES_USE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, homeNameSuggester::suggest)
                    .executesSuspending(taskExecutor, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(homesTracer) {
            val player = context.source.requirePlayer()
            val name = context.getArgument("name", String::class.java)

            val home = homeRepository.get(player.uniqueId, name)
            checkNotNull(home) { l10n.errorHomeNotFound(name) }

            val world = server.getWorld(home.world)
            checkNotNull(world) { l10n.errorWorldNotFound(home.world) }

            playerTeleporter.move(
                player,
                destination = home.toLocation(world),
                cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            )
            context.source.sender.sendMessage(
                l10n.teleportedToName(home.name),
            )
        }
}

// TODO: reuse
private fun Home.toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
