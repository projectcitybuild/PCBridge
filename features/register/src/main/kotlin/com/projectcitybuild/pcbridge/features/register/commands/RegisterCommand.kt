package com.projectcitybuild.pcbridge.features.register.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.features.register.registerTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.plugin.Plugin

class RegisterCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    internal var dialogFactory: (String) -> DialogLike = { email -> VerifyRegistrationCodeDialog.build(email) }

    override val build = command("register") {
        description = "Creates a new Project City Build account"
        argument("email", StringArgumentType.greedyString()) {
            executesSuspending(commandTaskExecutor, ::execute)
        }
        executes { context ->
            context.source.sender.sendMessage(l10n.registration.errorNoRegisterEmailSpecified)
            Command.SINGLE_SUCCESS
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(registerTracer) {
        val player = context.source.requirePlayer()
        val email = context.getArgument("email", String::class.java)

        registerHttpService.sendCode(
            email = email,
            playerAlias = player.name,
            playerUUID = player.uniqueId,
        )
        val dialog = dialogFactory(email)
        player.showDialog(dialog)
    }
}
