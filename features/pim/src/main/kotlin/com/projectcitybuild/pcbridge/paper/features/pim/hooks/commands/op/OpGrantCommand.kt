package com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op

import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.java.humanReadable
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.paper.features.pim.pimTracer
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class OpGrantCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
    private val localizedTime: LocalizedTime,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("grant")
            .requiresPermission(PermissionNode.PIM_OP)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: PaperCommandContext) =
        context.scoped(pimTracer) {
            val player = context.source.requirePlayer()

            val elevation = opElevationService.elevation(player.uniqueId)
            val now = localizedTime.nowInstant()
            val isActive = elevation != null && elevation.isActiveAt(now)
            if (isActive) {
                player.sendRichMessage(
                    "<red>Error: You are already OP elevated (remaining: ${elevation.remainingAt(now)?.humanReadable()}</red>",
                )
                return@scoped
            }

            val dialog = ConfirmOpElevateDialog.build()
            player.showDialog(dialog)
        }
}
