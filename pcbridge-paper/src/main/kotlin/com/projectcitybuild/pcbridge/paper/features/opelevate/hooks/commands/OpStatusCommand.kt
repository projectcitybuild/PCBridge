package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.java.humanReadable
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.event.ClickEvent.Payload.dialog
import org.bukkit.plugin.Plugin

class OpStatusCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
    private val localizedTime: LocalizedTime,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("opstatus")
            .requiresPermission(PermissionNode.OP_ELEVATE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(opElevateTracer) {
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