package com.projectcitybuild.pcbridge.paper.features.bans.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.bans.dialogs.CreateBanDialog
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val manageUrlGenerator: ManageUrlGenerator,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("ban")
            .requiresPermission(PermissionNode.BANS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val playerName = context.getArgument("player", String::class.java)

        val dialog = CreateBanDialog.build(playerName, onSubmit = { view, audience ->
            val playerName = view.getText(CreateBanDialog.playerNameKey)
            val reason = view.getText(CreateBanDialog.reasonKey)
            val additionalInfo = view.getText(CreateBanDialog.additionalInfoKey)
            logSync.info("Dialog response: playerName=[$playerName], reason=[$reason], additionalInfo=[$additionalInfo]")
        })
        context.source.sender.showDialog(dialog)

        val url = manageUrlGenerator.byPlayerUuid(
            playerName = playerName,
            path = "manage/player-bans/create"
        )

        val sender = context.source.sender
        sender.sendRichMessage(
            "<gray>Click the link below to create a ban for this player</gray>",
        )
        sender.sendRichMessage(
            "<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>",
        )
    }
}
