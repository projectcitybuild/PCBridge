package com.projectcitybuild.pcbridge.features.maintenance.hooks.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceState
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.features.maintenance.maintenanceTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.extensions.broadcastRich
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.argumenttypes.OnOffArgument
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.state.store.Store
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

    internal suspend fun toggle(context: PaperCommandContext) =
        context.scoped(maintenanceTracer) {
            val sender = context.source.sender

            val desiredState = context.getArgument("enabled", Boolean::class.java)
            val currentState = store.state(maintenanceStateKey).enabled

            if (currentState == desiredState) {
                sender.sendRichMessage(
                    "<red>Maintenance mode is already ${desiredState.onOff().uppercase()}</red>",
                )
                return@scoped
            }

            store.mutate(maintenanceStateKey) {
                MaintenanceState(enabled = desiredState)
            }
            eventBroadcaster.broadcast(
                MaintenanceToggledEvent(enabled = desiredState),
            )
            server.broadcastRich(
                "<yellow>Maintenance mode is now ${desiredState.onOff().uppercase()}</yellow>",
            )
        }

    internal suspend fun status(context: PaperCommandContext) =
        context.scoped(maintenanceTracer) {
            val sender = context.source.sender

            val state = store.state(maintenanceStateKey).enabled
            sender.sendRichMessage(
                "Maintenance mode is currently ${state.onOff().uppercase()}",
            )
        }
}

private fun Boolean.onOff(): String = if (this) "on" else "off"
