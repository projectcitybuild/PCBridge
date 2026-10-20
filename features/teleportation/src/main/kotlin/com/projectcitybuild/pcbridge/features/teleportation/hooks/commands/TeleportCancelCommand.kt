package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.plugin.Plugin

class TeleportCancelCommand(
    private val plugin: Plugin,
    private val application: TeleportService,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = command("cancel") {
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                application.cancel(context.source.requirePlayer())
            }
        }
    }
}
