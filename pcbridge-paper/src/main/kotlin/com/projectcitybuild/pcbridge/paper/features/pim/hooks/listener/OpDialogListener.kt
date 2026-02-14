package com.projectcitybuild.pcbridge.paper.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.paper.features.pim.pimTracer
import com.projectcitybuild.pcbridge.paper.features.pim.domain.services.OpElevationService
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class OpDialogListener(
    private val opElevationService: OpElevationService,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(
        event: PlayerCustomClickEvent,
    ) = event.scoped(pimTracer, this::class.java) {
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
            player.sendRichMessage("<red>Error: elevation reason is required</red>")
            return@scoped
        }

        try {
            opElevationService.elevate(player.uniqueId, reason)
        } catch (e: Exception) {
            player.sendRichMessage("<red>Error: ${e.message}</red>")
            throw e
        }
    }
}