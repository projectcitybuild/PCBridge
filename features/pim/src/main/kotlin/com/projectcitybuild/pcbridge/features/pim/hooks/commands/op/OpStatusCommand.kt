package com.projectcitybuild.pcbridge.features.pim.hooks.commands.op

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.core.support.java.humanReadable
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class OpStatusCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
    private val localizedTime: LocalizedTime,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("status")
            .requiresPermission(PermissionNode.PIM_OP)
            .executesSuspending(commandTaskExecutor, ::execute)
            .build()
    }

    suspend fun execute(context: PaperCommandContext) =
        context.scoped(pimTracer) {
            val player = context.source.requirePlayer()

            val elevation = opElevationService.elevation(player.uniqueId)
            if (elevation == null) {
                player.sendRichMessage("<gray>You are not currently OP elevated</gray>")
            } else {
                val now = localizedTime.nowInstant()
                player.sendRichMessage("<gray>You are OP elevated (remaining: ${elevation.remainingAt(now)?.humanReadable()})</gray>")
            }
        }
}
