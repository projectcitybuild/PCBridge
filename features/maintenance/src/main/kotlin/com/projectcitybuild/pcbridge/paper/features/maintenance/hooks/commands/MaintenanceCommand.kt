package com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.commands

import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.paper.features.maintenance.maintenanceTracer
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.platform.paper.extensions.broadcastRich
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.arguments.OnOffArgument
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import com.projectcitybuild.pcbridge.paper.runtime.state.store.Store
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class MaintenanceCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val store: Store,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("maintenance")
            .requiresPermission(PermissionNode.MAINTENANCE_MANAGE)
            .then(
                Commands.argument("enabled", OnOffArgument())
                    .executesSuspending(plugin, ::toggle),
            )
            .executesSuspending(plugin, ::status)
            .build()
    }

    private suspend fun toggle(context: PaperCommandContext) =
        context.scoped(maintenanceTracer) {
            val sender = context.source.sender

            val desiredState = context.getArgument("enabled", Boolean::class.java)
            val currentState = store.state.maintenance

            if (currentState == desiredState) {
                sender.sendRichMessage(
                    "<red>Maintenance mode is already ${desiredState.onOff().uppercase()}</red>",
                )
                return@scoped
            }

            store.mutate {
                store.state.copy(maintenance = desiredState)
            }
            eventBroadcaster.broadcast(
                MaintenanceToggledEvent(enabled = desiredState),
            )
            server.broadcastRich(
                "<yellow>Maintenance mode is now ${desiredState.onOff().uppercase()}</yellow>",
            )
        }

    private suspend fun status(context: PaperCommandContext) =
        context.scoped(maintenanceTracer) {
            val sender = context.source.sender

            val state = store.state.maintenance
            sender.sendRichMessage(
                "Maintenance mode is currently ${state.onOff().uppercase()}",
            )
        }
}

private fun Boolean.onOff(): String = if (this) "on" else "off"
