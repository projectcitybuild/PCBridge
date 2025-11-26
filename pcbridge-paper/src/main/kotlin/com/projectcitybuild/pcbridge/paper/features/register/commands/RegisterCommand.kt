package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class RegisterCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
) : BrigadierCommand {
    override val description: String = "Creates a new Project City Build account"

    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("register")
            .then(
                Commands.argument("email", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .executes { context ->
                context.source.sender.sendRichMessage(l10n.errorNoRegisterEmailSpecified)
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()
        val email = context.getArgument("email", String::class.java)

        registerHttpService.sendCode(
            email = email,
            playerAlias = player.name,
            playerUUID = player.uniqueId,
        )
        val dialog = VerifyRegistrationCodeDialog.build(
            email = email,
        )
        player.showDialog(dialog)
    }
}
