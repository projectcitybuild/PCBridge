package com.projectcitybuild.pcbridge.features.homes.hooks.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Home
import com.projectcitybuild.pcbridge.features.homes.HomesPermissions
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.homesTracer
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

class HomeCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val homeNameSuggester: HomeNameSuggester,
    private val homeRepository: HomeRepository,
    private val playerTeleporter: PlayerTeleporter,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val build = command("home") {
        description = "Teleports to one of your homes"
        requiresPermission(HomesPermissions.USE)
        argument("name", StringArgumentType.greedyString()) {
            suggestsSuspending(plugin, homeNameSuggester::suggest)
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(homesTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val home = homeRepository.get(player.uniqueId, name)
        checkNotNull(home) { l10n.homes.errorHomeNotFound(name) }

        val world = server.getWorld(home.world)
        checkNotNull(world) { l10n.errors.errorWorldNotFound(home.world) }

        playerTeleporter.move(
            player,
            destination = home.toLocation(world),
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        context.source.sender.sendMessage(
            l10n.teleportation.teleportedToName(home.name),
        )
    }
}

// TODO: reuse
private fun Home.toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
