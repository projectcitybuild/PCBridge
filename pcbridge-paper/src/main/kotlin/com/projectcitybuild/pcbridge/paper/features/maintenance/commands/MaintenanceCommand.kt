package com.projectcitybuild.pcbridge.paper.features.maintenance.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.core.extensions.onOff
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnOffArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.maintenance.events.MaintenanceToggledEvent
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class MaintenanceCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val store: Store,
    private val eventBroadcaster: SpigotEventBroadcaster,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("maintenance")
            .requiresPermission(PermissionNode.MAINTENANCE_MANAGE)
            .then(
                Commands.argument("enabled", OnOffArgument())
                    .executesSuspending(plugin, ::toggle)
            )
            .executesSuspending(plugin, ::status)
            .build()
    }

    private suspend fun toggle(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val sender = context.source.sender

        val desiredState = context.getArgument("enabled", Boolean::class.java)
        val currentState = store.state.maintenance

        if (currentState == desiredState) {
            sender.sendMessage(
                miniMessage.deserialize("<red>Maintenance mode is already ${desiredState.onOff().uppercase()}</red>")
            )
            return@traceSuspending
        }

        store.mutate {
            store.state.copy(maintenance = desiredState)
        }
        eventBroadcaster.broadcast(
            MaintenanceToggledEvent(enabled = desiredState)
        )
        server.broadcast(
            miniMessage.deserialize("<yellow>Maintenance mode is now ${desiredState.onOff().uppercase()}</yellow>")
        )
    }

    private suspend fun status(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val sender = context.source.sender

        val state = store.state.maintenance
        sender.sendMessage(
            miniMessage.deserialize("Maintenance mode is currently ${state.onOff().uppercase()}")
        )
    }
}
