package com.projectcitybuild.pcbridge.paper.features.bans.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.support.component.sendMessageRich
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.broadcastRich
import com.projectcitybuild.pcbridge.paper.features.bans.banTracer
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CreateUuidBan
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.dialogs.CreateBanDialog
import com.projectcitybuild.pcbridge.paper.features.bans.domain.utilities.toMiniMessage
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

class BanDialogListener(
    private val server: Server,
    private val createUuidBan: CreateUuidBan,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(
        event: PlayerCustomClickEvent,
    ) = event.scoped(banTracer, this::class.java) {
        if (! event.identifier.equals(CreateBanDialog.submitBanButtonKey)) return@scoped

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@scoped
        }

        val playerName = view.getText(CreateBanDialog.playerNameKey)?.trim()
        val reason = view.getText(CreateBanDialog.reasonKey)?.trim()
        val additionalInfo = view.getText(CreateBanDialog.additionalInfoKey)?.trim()

        logSync.info("Dialog response received", mapOf(
            "player_name" to playerName,
            "reason" to reason,
            "additional_info" to additionalInfo,
        ))

        val connection = event.commonConnection as? PlayerGameConnection
        val bannerPlayer = connection?.player

        if (playerName.isNullOrEmpty() || reason.isNullOrEmpty()) {
            val dialog = CreateBanDialog.build(
                playerName,
                reason,
                additionalInfo,
                error = if (playerName.isNullOrEmpty()) "Error: Player name cannot be empty"
                    else if (reason.isNullOrEmpty()) "Error: Reason cannot be empty"
                    else "",
            )
            bannerPlayer?.showDialog(dialog)
            return@scoped
        }

        val result = try {
            createUuidBan.create(
                bannedAlias = playerName,
                bannerUuid = bannerPlayer?.uniqueId,
                bannerAlias = bannerPlayer?.name,
                reason = reason,
                additionalInfo = additionalInfo,
            )
        } catch (_: CreateUuidBan.PlayerNotFound) {
            bannerPlayer?.sendMessageRich("<red>Error: Player not found</red>")
            return@scoped
        } catch (_: CreateUuidBan.PlayerAlreadyBanned) {
            bannerPlayer?.sendMessageRich("<red>Error: $playerName is already banned</red>")
            return@scoped
        } catch (e: CreateUuidBan.InvalidBanInput) {
            bannerPlayer?.sendMessageRich("<red>Error: ${e.message}</red>")
            return@scoped
        }

        server.broadcastRich(l10n.playerHasBeenBanned(playerName))

        val bannedPlayer = server.onlinePlayers.firstOrNull { it.uniqueId == result.bannedUuid }
        bannedPlayer?.kick(
            result.ban.toMiniMessage(),
            PlayerKickEvent.Cause.BANNED,
        )
        bannerPlayer?.sendMessageRich(l10n.clickToEditBan(result.editUrl))
    }
}