package com.projectcitybuild.pcbridge.paper.features.warps.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.dialogs.WarpRenameDialog
import com.projectcitybuild.pcbridge.paper.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WarpRenameDialogListener(
    private val warpRepository: WarpRepository,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(
        event: PlayerCustomClickEvent,
    ) = event.scoped(warpsTracer, this::class.java) {
        if (! event.identifier.equals(WarpRenameDialog.saveButtonKey)) return@scoped

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@scoped
        }

        val warpId = view.getText(WarpRenameDialog.idKey)
        val newName = view.getText(WarpRenameDialog.newNameKey)?.trim()

        logSync.info("Dialog response received", mapOf(
            "new_name" to newName,
            "warp_id" to warpId,
        ))

        val connection = event.commonConnection as? PlayerGameConnection
        val player = connection?.player ?: return@scoped

        if (newName.isNullOrEmpty()) {
            player.sendRichMessage("<red>Error: New name cannot be empty</red>")
            return@scoped
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
    }
}