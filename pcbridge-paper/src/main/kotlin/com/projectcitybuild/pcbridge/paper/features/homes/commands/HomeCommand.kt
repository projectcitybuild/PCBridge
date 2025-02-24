package com.projectcitybuild.pcbridge.paper.features.homes.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
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

class HomeCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val server: Server,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("home")
            .requiresPermission(PermissionNode.HOMES_USE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        // val miniMessage = MiniMessage.miniMessage()
        // val name = context.getArgument("name", String::class.java)
        //
        // val build = buildRepository.get(name = name)
        // checkNotNull(build) { "Build not found" }
        //
        // val world = server.getWorld(build.world)
        // checkNotNull(world) { "Could not find world ${build.world}" }
        //
        // val location = Location(
        //     world,
        //     build.x,
        //     build.y,
        //     build.z,
        //     build.yaw,
        //     build.pitch,
        // )
        //
        // val executor = context.source.executor
        // if (executor is Player) {
        //     server.pluginManager.callEvent(
        //         PlayerPreWarpEvent(executor),
        //     )
        // }
        //
        // val didTeleport = executor?.teleportAsync(
        //     location,
        //     PlayerTeleportEvent.TeleportCause.COMMAND,
        // )?.await()
        //
        // if (didTeleport != true) return@traceSuspending
        //
        // val owner = build.player?.alias
        //
        // executor.showTitle(
        //     Title.title(
        //         Component.text(build.name),
        //         miniMessage.deserialize(
        //             if (owner.isNullOrEmpty()) ""
        //             else "<gray>Created by $owner</gray>"
        //         ),
        //     )
        // )
        // context.source.sender.sendMessage(
        //     miniMessage.deserialize("<gray>Teleported to <aqua>$name</aqua></gray>")
        // )
        // if (!build.description.isNullOrEmpty()) {
        //     executor.sendMessage(
        //         miniMessage.deserialize("<gray>---<newline>${build.description}</gray>")
        //     )
        // }
        // if (!build.lore.isNullOrEmpty()) {
        //     executor.sendMessage(
        //         miniMessage.deserialize("<gray>---<newline><italic>${build.lore}</italic></gray>")
        //     )
        // }
    }
}
