package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.paper.features.register.registerTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class CodeCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override val description: String = "Finishes account registration by verifying a code"

    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("code")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(registerTracer) {
        val player = context.source.requirePlayer()

        val dialog = VerifyRegistrationCodeDialog.build(email = null)
        player.showDialog(dialog)
    }
}
