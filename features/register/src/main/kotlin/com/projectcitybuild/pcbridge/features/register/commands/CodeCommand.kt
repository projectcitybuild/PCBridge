package com.projectcitybuild.pcbridge.features.register.commands

import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.features.register.registerTracer
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.plugin.Plugin

class CodeCommand(
    private val plugin: Plugin,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    internal var dialogFactory: () -> DialogLike = { VerifyRegistrationCodeDialog.build(email = null) }

    override val description: String = "Finishes account registration by verifying a code"

    override fun literal(): PaperCommandNode {
        return Commands.literal("code")
            .executesSuspending(commandTaskExecutor, ::execute)
            .build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(registerTracer) {
        val player = context.source.requirePlayer()

        val dialog = dialogFactory()
        player.showDialog(dialog)
    }
}
