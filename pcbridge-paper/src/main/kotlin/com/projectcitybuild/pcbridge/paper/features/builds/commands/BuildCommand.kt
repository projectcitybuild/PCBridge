package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val server: Server,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("build")
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

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val name = context.getArgument("name", String::class.java)

        val build = buildRepository.get(name = name)
        checkNotNull(build) { "Build not found" }

        val world = server.getWorld(build.world)
        checkNotNull(world) { "Could not find world {$build.world}" }

        context.source.sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<gray>Teleporting to $name...</gray>")
        )
        val location = Location(
            world,
            build.x,
            build.y,
            build.z,
            build.yaw,
            build.pitch,
        )

        if (context.source.executor is Player) {
            server.pluginManager.callEvent(
                PlayerPreWarpEvent(context.source.executor as Player),
            )
        }

        val didTeleport = context.source.executor?.teleportAsync(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        )?.await()

        if (didTeleport == true) {
            context.source.executor?.showTitle(
                Title.title(
                    Component.text(build.name),
                    Component.text("TODO"),
                )
            )
        }
    }
}
