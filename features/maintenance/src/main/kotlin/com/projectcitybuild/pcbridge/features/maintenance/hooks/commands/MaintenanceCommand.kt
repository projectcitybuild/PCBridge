package com.projectcitybuild.pcbridge.features.maintenance.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.maintenance.MaintenancePermissions
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceState
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.features.maintenance.maintenanceTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnOffArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class MaintenanceCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val store: Store,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = command("maintenance") {
        requiresPermission(MaintenancePermissions.MANAGE)
        argument("enabled", OnOffArgument()) {
            executesSuspending(taskExecutor, ::toggle)
        }
        executesSuspending(taskExecutor, ::status)
    }

    private suspend fun toggle(context: PaperCommandContext) = context.scoped(maintenanceTracer) {
        val sender = context.source.sender

        val desiredState = context.getArgument("enabled", Boolean::class.java)
        val currentState = store.state(maintenanceStateKey).enabled

        if (currentState == desiredState) {
            sender.sendMessage(l10n.maintenance.maintenanceAlreadyInState(desiredState))
            return@scoped
        }

        store.mutate(maintenanceStateKey) {
            MaintenanceState(enabled = desiredState)
        }
        eventBroadcaster.broadcast(
            MaintenanceToggledEvent(enabled = desiredState),
        )
        server.broadcast(l10n.maintenance.maintenanceModeChanged(desiredState))
    }

    private suspend fun status(context: PaperCommandContext) = context.scoped(maintenanceTracer) {
        val sender = context.source.sender

        val state = store.state(maintenanceStateKey).enabled
        sender.sendMessage(l10n.maintenance.maintenanceStatus(state))
    }
}
