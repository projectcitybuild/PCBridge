package com.projectcitybuild.pcbridge.paper.features.bans.commands

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.pcbridge.http.pcb.services.UuidBanHttpService
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
import com.projectcitybuild.pcbridge.paper.features.bans.repositories.UuidBanRepository
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.sentry.kotlin.SentryContext
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val manageUrlGenerator: ManageUrlGenerator,
    private val uuidBanRepository: UuidBanRepository,
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
        val rawPlayerName = context.getArgument("player", String::class.java)
        val onlinePlayer = server.onlinePlayers.firstOrNull { it.name == rawPlayerName }
        val playerName = onlinePlayer?.name ?: rawPlayerName

        val dialog = CreateBanDialog.build(playerName, onSubmit = ::submitDialog)
        context.source.sender.showDialog(dialog)

//        val url = manageUrlGenerator.byPlayerUuid(
//            playerName = playerName,
//            path = "manage/player-bans/create"
//        )
//
//        val sender = context.source.sender
//        sender.sendRichMessage(
//            "<gray>Click the link below to create a ban for this player</gray>",
//        )
//        sender.sendRichMessage(
//            "<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>",
//        )
    }

    private fun submitDialog(view: DialogResponseView, audience: Audience) {
        val playerName = view.getText(CreateBanDialog.playerNameKey)
        val reason = view.getText(CreateBanDialog.reasonKey)
        val additionalInfo = view.getText(CreateBanDialog.additionalInfoKey)
        logSync.info("Dialog response: playerName=[$playerName], reason=[$reason], additionalInfo=[$additionalInfo]")

        if (playerName.isNullOrEmpty()) {
            val dialog = CreateBanDialog.build(
                playerName,
                reason,
                additionalInfo,
                error = "Error: Player name cannot be empty",
                onSubmit = ::submitDialog
            )
            audience.showDialog(dialog)
            return
        }
        if (reason.isNullOrEmpty()) {
            val dialog = CreateBanDialog.build(
                playerName,
                reason,
                additionalInfo,
                error = "Error: Reason cannot be empty",
                onSubmit = ::submitDialog
            )
            audience.showDialog(dialog)
            return
        }
        // TODO: make reusable class to inject launcher
        plugin.launch(plugin.minecraftDispatcher + SentryContext() + object : CoroutineTimings() {}) {
            val banner = audience as? Player
            val uuid = UUID.randomUUID() // TODO

            uuidBanRepository.create(
                bannedUUID = uuid,
                bannedAlias = playerName,
                bannerUUID = banner?.uniqueId,
                bannerAlias = banner?.name,
                reason = reason,
                additionalInfo = additionalInfo,
            )
            server.broadcast(Component.text("$playerName has been banned"))

            val player = server.onlinePlayers.firstOrNull { it.uniqueId == uuid }
            player?.kick(
                Component.text("TODO"),
                PlayerKickEvent.Cause.BANNED,
            )
        }
    }
}
