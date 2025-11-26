package com.projectcitybuild.pcbridge.paper.features.register.listeners

import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class VerifyCodeDialogListener(
    private val registerHttpService: RegisterHttpService,
    private val errorTracker: ErrorTracker,
) : Listener {
    @EventHandler
    // TODO: reusable sentry and error handling
    suspend fun onPlayerCustomClickEvent(event: PlayerCustomClickEvent) = runCatching {
        if (! event.identifier.equals(VerifyRegistrationCodeDialog.verifyButtonKey)) return@runCatching

        val view = event.dialogResponseView
        if (view == null) {
            log.error { "DialogResponseView was null for ${event.identifier}" }
            return@runCatching
        }

        val code = view.getText(VerifyRegistrationCodeDialog.codeKey)?.trim()
        logSync.info("Dialog response: code=[$code]")

        val connection = event.commonConnection as? PlayerGameConnection
        val player = connection?.player ?: return@runCatching

        if (code.isNullOrEmpty()) {
            val dialog = VerifyRegistrationCodeDialog.build(
                email = null,
                error = l10n.errorNoCodeSpecified,
            )
            player.showDialog(dialog)
            return@runCatching
        }

        try {
            registerHttpService.verifyCode(
                code = code,
                playerUUID = player.uniqueId,
            )
            player.sendRichMessage(l10n.registrationComplete)
        } catch (_: ResponseParserError.NotFound) {
            val dialog = VerifyRegistrationCodeDialog.build(
                email = null,
                error = l10n.errorCodeInvalidOrExpired,
            )
            player.showDialog(dialog)
            return@runCatching
        }
    }.onFailure {
        log.error(it) { "Failed to handle ban dialog response" }
        errorTracker.report(it)
    }
}