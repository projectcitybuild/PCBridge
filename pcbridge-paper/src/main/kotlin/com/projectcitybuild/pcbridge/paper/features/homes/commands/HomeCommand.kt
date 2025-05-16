package com.projectcitybuild.pcbridge.paper.features.homes.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.pcb.models.Home
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class HomeCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val homeRepository: HomeRepository,
    private val playerTeleporter: PlayerTeleporter,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("home")
            .requiresPermission(PermissionNode.HOMES_USE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, ::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun suggest(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val player = context.source.executor as? Player
            ?: return

        val input = suggestions.remaining.lowercase()

        homeRepository.names(playerUUID = player.uniqueId)
            .filter { it.name.startsWith(input) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val name = context.getArgument("name", String::class.java)

        val player = context.source.sender as? Player
        checkNotNull(player) { "Only players can use this command" }

        val home = homeRepository.get(player.uniqueId, name)
        checkNotNull(home) { "Home ($home) not found" }

        val world = server.getWorld(home.world)
        checkNotNull(world) { "Could not find world ${home.world}" }

        playerTeleporter.move(
            player,
            destination = home.toLocation(world),
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        context.source.sender.sendRichMessage(
            "<gray>Teleported to <aqua>$name</aqua></gray>",
        )
    }
}

// TODO: reuse
private fun Home.toLocation(world: World)
    = Location(world, x, y, z, yaw, pitch)
