package com.projectcitybuild.pcbridge.features.register.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.runtime.listeners.scoped
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.features.register.registerTracer
import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.core.l10n.l10n
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.event.player.PlayerCustomClickEvent
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class VerifyCodeDialogListener(
    private val registerHttpService: RegisterHttpService,
    private val syncPlayer: SyncPlayer,
) : Listener {
    internal var errorDialogFactory: (Component) -> DialogLike = { error ->
        VerifyRegistrationCodeDialog.build(email = null, error = error)
    }

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
            val dialog = errorDialogFactory(l10n.errorNoCodeSpecified)
            player.showDialog(dialog)
            return@scoped
        }

        try {
            registerHttpService.verifyCode(
                code = code,
                playerUUID = player.uniqueId,
            )
            player.sendMessage(l10n.registrationComplete)
            syncPlayer.execute(playerUUID = player.uniqueId)

        } catch (_: ResponseParserError.NotFound) {
            val dialog = errorDialogFactory(l10n.errorCodeInvalidOrExpired)
            player.showDialog(dialog)
            return@scoped
        }
    }
}