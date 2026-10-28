package com.projectcitybuild.pcbridge.features.warps.hooks.listeners

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.hooks.dialogs.WarpRenameDialog
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.runtime.listeners.traced
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class WarpRenameDialogListener(
    private val warpRepository: WarpRepository,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(event: PlayerCustomClickEvent) =
        event.traced(warpsTracer, this::class.java) {
            if (!event.identifier.equals(WarpRenameDialog.saveButtonKey)) return@traced

            val view = event.dialogResponseView
            if (view == null) {
                log.error { "DialogResponseView was null for ${event.identifier}" }
                return@traced
            }

            val warpId = view.getText(WarpRenameDialog.idKey)
            val newName = view.getText(WarpRenameDialog.newNameKey)?.trim()

            logSync.info(
                "Dialog response received",
                mapOf(
                    "new_name" to newName,
                    "warp_id" to warpId,
                ),
            )

            val connection = event.commonConnection as? PlayerGameConnection
            val player = connection?.player ?: return@traced

            if (newName.isNullOrEmpty()) {
                player.sendMessage(l10n.errors.errorNewNameCannotBeEmpty)
                return@traced
            }
            if (warpId.isNullOrEmpty()) {
                player.sendMessage(l10n.warps.errorRenameWarpNotFound)
                throw Exception("Rename failed: warp id was null")
            }
            try {
                warpRepository.rename(
                    id = warpId.toInt(),
                    newName = newName,
                )
                player.sendMessage(l10n.warps.warpRenamed(newName))
            } catch (e: Exception) {
                player.sendMessage(l10n.errors.errorWithMessage(e.message))
                throw e
            }
        }
}
