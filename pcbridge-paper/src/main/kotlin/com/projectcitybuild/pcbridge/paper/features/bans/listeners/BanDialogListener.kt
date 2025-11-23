package com.projectcitybuild.pcbridge.paper.features.bans.listeners

import com.projectcitybuild.pcbridge.http.playerdb.services.PlayerDbMinecraftService
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.support.component.sendMessageRich
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.broadcastRich
import com.projectcitybuild.pcbridge.paper.features.bans.dialogs.CreateBanDialog
import com.projectcitybuild.pcbridge.paper.features.bans.repositories.UuidBanRepository
import com.projectcitybuild.pcbridge.paper.features.bans.utilities.toMiniMessage
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import java.util.UUID

class BanDialogListener(
    private val server: Server,
    private val playerDbMinecraftService: PlayerDbMinecraftService,
    private val uuidBanRepository: UuidBanRepository,
    private val manageUrlGenerator: ManageUrlGenerator,
    private val errorTracker: ErrorTracker,
) : Listener {
    @EventHandler
    // TODO: reusable sentry and error handling
    suspend fun onPlayerCustomClickEvent(event: PlayerCustomClickEvent) = runCatching {
        if (! event.identifier.equals(CreateBanDialog.submitBanButtonKey)) return@runCatching

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@runCatching
        }

        val playerName = view.getText(CreateBanDialog.playerNameKey)?.trim()
        val reason = view.getText(CreateBanDialog.reasonKey)?.trim()
        val additionalInfo = view.getText(CreateBanDialog.additionalInfoKey)?.trim()
        logSync.info("Dialog response: playerName=[$playerName], reason=[$reason], additionalInfo=[$additionalInfo]")

        val connection = event.commonConnection as? PlayerGameConnection
        val bannerPlayer = connection?.player

        if (playerName.isNullOrEmpty()) {
            val dialog = CreateBanDialog.build(
                playerName,
                reason,
                additionalInfo,
                error = "Error: Player name cannot be empty",
            )
            bannerPlayer?.showDialog(dialog)
            return@runCatching
        }
        if (reason.isNullOrEmpty()) {
            val dialog = CreateBanDialog.build(
                playerName,
                reason,
                additionalInfo,
                error = "Error: Reason cannot be empty",
            )
            bannerPlayer?.showDialog(dialog)
            return@runCatching
        }

        val playerLookup = playerDbMinecraftService.player(playerName).data
        if (playerLookup == null) {
            bannerPlayer?.sendMessageRich("<red>Error: Player $playerName not found</red>")
            return@runCatching
        }
        val rawUuid = playerLookup.player.id
        val uuid = try {
            UUID.fromString(rawUuid)
        } catch (e: Exception) {
            bannerPlayer?.sendMessageRich("<red>Error: Could not determine player UUID</red>")
            log.error(e) { "Could not parse UUID: $rawUuid, player=[$playerName]" }
            return@runCatching
        }

        val ban = try {
            uuidBanRepository.create(
                bannedUUID = uuid,
                bannedAlias = playerName,
                bannerUUID = bannerPlayer?.uniqueId,
                bannerAlias = bannerPlayer?.name,
                reason = reason,
                additionalInfo = additionalInfo,
            )
        } catch (e: ResponseParserError.Conflict) {
            bannerPlayer?.sendMessageRich("<red>Error: ${e.message}</red>")
            return@runCatching
        } catch (e: ResponseParserError.Validation) {
            bannerPlayer?.sendMessageRich("<red>Error: ${e.message}</red>")
            return@runCatching
        }

        server.broadcastRich(l10n.playerHasBeenBanned(playerName))

        val bannedPlayer = server.onlinePlayers.firstOrNull { it.uniqueId == uuid }
        bannedPlayer?.kick(
            ban.toMiniMessage(),
            PlayerKickEvent.Cause.BANNED,
        )

        val url = manageUrlGenerator.byPlayerUuid(
            playerName = playerName,
            path = "manage/player-bans/${ban.id}/edit"
        )
        bannerPlayer?.sendMessageRich(l10n.clickToEditBan(url))
    }.onFailure {
        log.error(it) { "Failed to handle ban dialog response" }
        errorTracker.report(it)
    }
}