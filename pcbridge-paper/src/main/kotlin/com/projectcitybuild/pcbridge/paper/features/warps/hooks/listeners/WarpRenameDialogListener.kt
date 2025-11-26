package com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.dialogs.WarpRenameDialog
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WarpRenameDialogListener(
    private val warpRepository: WarpRepository,
    private val errorTracker: ErrorTracker,
) : Listener {
    @EventHandler
    // TODO: reusable sentry and error handling
    suspend fun onPlayerCustomClickEvent(event: PlayerCustomClickEvent) = runCatching {
        if (! event.identifier.equals(WarpRenameDialog.saveButtonKey)) return@runCatching

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@runCatching
        }

        val warpId = view.getText(WarpRenameDialog.idKey)
        val newName = view.getText(WarpRenameDialog.newNameKey)?.trim()
        logSync.info("Dialog response: newName=[$newName], warpId=[$warpId]")

        val connection = event.commonConnection as? PlayerGameConnection
        val player = connection?.player ?: return@runCatching

        if (newName.isNullOrEmpty()) {
            player.sendRichMessage("<red>Error: New name cannot be empty</red>")
            return@runCatching
        }
        if (warpId.isNullOrEmpty()) {
            player.sendRichMessage("<red>Error: Warp not found</red>")
            throw Exception("Rename failed: warp id was null")
        }
        try {
            warpRepository.rename(
                id = warpId.toInt(),
                newName = newName,
            )
            player.sendRichMessage(l10n.warpRenamed(newName))
        } catch (e: Exception) {
            player.sendRichMessage("<red>Error: ${e.message}</red>")
            throw e
        }
    }.onFailure {
        log.error(it) { "Failed to handle ban dialog response" }
        errorTracker.report(it)
    }
}