package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.features.opelevate.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.event.ClickEvent.Payload.dialog
import org.bukkit.plugin.Plugin

class OpMeCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("opme")
            .requiresPermission(PermissionNode.OP_ELEVATE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(opElevateTracer) {
        val player = context.source.requirePlayer()

        if (opElevationService.isElevated(player.uniqueId)) {
            // TODO
            player.sendRichMessage("<red>Error: You are already OP elevated (remaining: TODO</red>")
            return@scoped
        }

        val dialog = ConfirmOpElevateDialog.build()
        player.showDialog(dialog)
    }
}