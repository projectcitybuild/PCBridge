package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.services.TeleportService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import org.bukkit.plugin.Plugin

class TeleportRandomCommand(
    private val plugin: Plugin,
    private val application: TeleportService,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = command("random") {
        requiresPermission(TeleportationPermissions.RANDOM)
        executesSuspending(plugin) { context ->
            context.scoped(teleportationTracer) {
                application.random(context.source.requirePlayer())
            }
        }
    }
}
