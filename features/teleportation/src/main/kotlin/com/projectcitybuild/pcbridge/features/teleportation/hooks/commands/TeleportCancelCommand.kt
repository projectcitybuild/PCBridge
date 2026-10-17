package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands

import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportApplication
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.teleportationTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class TeleportCancelCommand(
    private val plugin: Plugin,
    private val application: TeleportApplication,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode =
        Commands.literal("cancel")
            .executesSuspending(plugin) { context ->
                context.scoped(teleportationTracer) {
                    application.cancel(context.source.requirePlayer())
                }
            }
            .build()
}
