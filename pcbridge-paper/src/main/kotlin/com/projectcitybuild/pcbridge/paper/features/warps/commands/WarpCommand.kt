package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.models.pcb.Warp
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class WarpCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val server: Server,
) : BrigadierCommand {
    override val description: String = "Teleports to a warp"

    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("warp")
            .requiresPermission(PermissionNode.WARP_TELEPORT)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggestsSuspending(plugin, ::suggestWarp)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun suggestWarp(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val name = suggestions.remaining.lowercase()

        return warpRepository.all()
            .filter { it.name.lowercase().startsWith(name) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val warpName = context.getArgument("name", String::class.java)
        val executor = context.source.executor
        val player = executor as? Player
        checkNotNull(player) { "Only players can use this command" }

        val warp = warpRepository.get(name = warpName)
        checkNotNull(warp) { "Warp $warpName not found" }

        val world = server.getWorld(warp.world)
        checkNotNull(world) { "World ${warp.world} does not exist" }

        val location = warp.toLocation(world)

        server.pluginManager.callEvent(
            PlayerPreWarpEvent(player),
        )

        player.teleportAsync(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        ).await()

        executor.sendMessage(
            Component.text("Warped to ${warp.name}")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC),
        )
    }
}

private fun Warp.toLocation(world: World) = Location(
    world,
    x,
    y,
    z,
    yaw,
    pitch,
)