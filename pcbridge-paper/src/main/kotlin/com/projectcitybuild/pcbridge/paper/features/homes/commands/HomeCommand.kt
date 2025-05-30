package com.projectcitybuild.pcbridge.paper.features.homes.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.pcb.models.Home
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
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
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("home")
            .requiresPermission(PermissionNode.HOMES_USE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, homeNameSuggester::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
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
        context.source.sender.sendRichMessage(
            l10n.teleportedToName(home.name),
        )
    }
}

// TODO: reuse
private fun Home.toLocation(world: World)
    = Location(world, x, y, z, yaw, pitch)
