package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.runtime.listeners.scoped
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class OpDialogListener(
    private val opElevationService: OpElevationService,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(event: PlayerCustomClickEvent) =
        event.scoped(pimTracer, this::class.java) {
            if (event.identifier != ConfirmOpElevateDialog.proceedButtonKey) return@scoped

            val view = event.dialogResponseView
            if (view == null) {
                log.error { "DialogResponseView was null for ${event.identifier}" }
                return@scoped
            }

            val reason = view.getText(ConfirmOpElevateDialog.REASON_KEY)?.trim()

            logSync.info("Dialog response received", mapOf("reason" to reason))

            val connection = event.commonConnection as? PlayerGameConnection
            val player = connection?.player ?: return@scoped

            if (reason.isNullOrEmpty()) {
                player.sendMessage(l10n.pim.errorOpElevationReasonRequired)
                return@scoped
            }

            try {
                opElevationService.elevate(player.uniqueId, reason)
            } catch (e: Exception) {
                player.sendMessage(l10n.errors.errorWithMessage(e.message))
                throw e
            }
        }
}
