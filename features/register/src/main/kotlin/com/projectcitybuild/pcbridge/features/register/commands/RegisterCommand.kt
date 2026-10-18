package com.projectcitybuild.pcbridge.features.register.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
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
import com.projectcitybuild.pcbridge.core.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.plugin.Plugin

class RegisterCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    internal var dialogFactory: (String) -> DialogLike = { email -> VerifyRegistrationCodeDialog.build(email) }

    override val description: String = "Creates a new Project City Build account"

    override fun literal(): PaperCommandNode {
        return Commands.literal("register")
            .then(
                Commands.argument("email", StringArgumentType.greedyString())
                    .executesSuspending(commandTaskExecutor, ::execute)
            )
            .executes { context ->
                context.source.sender.sendMessage(l10n.errorNoRegisterEmailSpecified)
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

    private suspend fun execute(
        context: PaperCommandContext,
    ) = context.scoped(registerTracer) {
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
