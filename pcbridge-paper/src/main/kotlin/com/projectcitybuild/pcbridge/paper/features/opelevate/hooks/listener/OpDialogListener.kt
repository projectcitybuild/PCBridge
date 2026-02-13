package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.http.pcb.services.OpElevateHttpService
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.opelevate.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import com.projectcitybuild.pcbridge.paper.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import net.kyori.adventure.text.event.ClickEvent.Payload.dialog
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class OpDialogListener(
    private val opElevateHttpService: OpElevateHttpService,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(
        event: PlayerCustomClickEvent,
    ) = event.scoped(opElevateTracer, this::class.java) {
        if (event.identifier != ConfirmOpElevateDialog.proceedButtonKey) return@scoped

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@scoped
        }

        val reason = view.getText(ConfirmOpElevateDialog.reasonKey)?.trim()

        logSync.info("Dialog response received", mapOf("reason" to reason))

        val connection = event.commonConnection as? PlayerGameConnection
        val player = connection?.player ?: return@scoped

        if (reason.isNullOrEmpty()) {
            player.sendRichMessage("<red>Error: reason is required</red>")
            return@scoped
        }

        try {
            val elevation = opElevateHttpService.start(
                playerUUID = player.uniqueId,
                reason = reason,
            )
            player.isOp = true
            player.sendRichMessage("OP granted (remaining: TODO)")
        } catch (e: Exception) {
            player.sendRichMessage("<red>Error: ${e.message}</red>")
            throw e
        }
    }
}