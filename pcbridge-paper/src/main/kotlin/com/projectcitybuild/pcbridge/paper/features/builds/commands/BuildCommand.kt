package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.pcb.models.Build
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class BuildCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val server: Server,
    private val playerTeleporter: PlayerTeleporter,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("build")
            .requiresPermission(PermissionNode.BUILDS_TELEPORT)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, ::suggestBuild)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun suggestBuild(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val name = suggestions.remaining.lowercase()

        buildRepository.names()
            .filter { it.lowercase().startsWith(name) }
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val name = context.getArgument("name", String::class.java)

        val player = context.source.sender as? Player
        checkNotNull(player) { "Only players can use this command" }

        val build = buildRepository.get(name = name)
        checkNotNull(build) { "Build not found" }

        val world = server.getWorld(build.world)
        checkNotNull(world) { "Could not find world ${build.world}" }

        playerTeleporter.move(
            player,
            destination = build.toLocation(world),
            cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            options = PlayerTeleporter.TeleportOptions(
                preloadDestinationChunk = true,
            ),
        )
        val owner = build.player?.alias

        player.showTitle(
            Title.title(
                Component.text(build.name),
                miniMessage.deserialize(
                    if (owner.isNullOrEmpty()) ""
                    else "<gray>Created by $owner</gray>"
                ),
            )
        )
        context.source.sender.sendMessage(
            miniMessage.deserialize("<gray>Teleported to <aqua>$name</aqua></gray>")
        )
        if (!build.description.isNullOrEmpty()) {
            player.sendMessage(
                miniMessage.deserialize("<gray>---<newline>${build.description}</gray>")
            )
        }
        if (!build.lore.isNullOrEmpty()) {
            player.sendMessage(
                miniMessage.deserialize("<gray>---<newline><italic>${build.lore}</italic></gray>")
            )
        }
    }
}

// TODO: reuse
private fun Build.toLocation(world: World)
    = Location(world, x, y, z, yaw, pitch)