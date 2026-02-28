package com.projectcitybuild.pcbridge.paper.features.register.listeners

import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.paper.features.register.registerTracer
import com.projectcitybuild.pcbridge.paper.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class VerifyCodeDialogListener(
    private val registerHttpService: RegisterHttpService,
    private val syncPlayer: SyncPlayer,
) : Listener {
    @EventHandler
    suspend fun onPlayerCustomClickEvent(
        event: PlayerCustomClickEvent,
    ) = event.scoped(registerTracer, this::class.java) {
        if (event.identifier != VerifyRegistrationCodeDialog.verifyButtonKey) return@scoped

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@scoped
        }

        val code = view.getText(VerifyRegistrationCodeDialog.codeKey)?.trim()

        logSync.info("Dialog response received", mapOf("code" to code))

        val connection = event.commonConnection as? PlayerGameConnection
        val player = connection?.player ?: return@scoped

        if (code.isNullOrEmpty()) {
            val dialog = VerifyRegistrationCodeDialog.build(
                email = null,
                error = l10n.errorNoCodeSpecified,
            )
            player.showDialog(dialog)
            return@scoped
        }

        try {
            registerHttpService.verifyCode(
                code = code,
                playerUUID = player.uniqueId,
            )
            player.sendRichMessage(l10n.registrationComplete)
            syncPlayer.execute(playerUUID = player.uniqueId)

        } catch (_: ResponseParserError.NotFound) {
            val dialog = VerifyRegistrationCodeDialog.build(
                email = null,
                error = l10n.errorCodeInvalidOrExpired,
            )
            player.showDialog(dialog)
            return@scoped
        }
    }
}