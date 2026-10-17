package com.projectcitybuild.pcbridge.features.register.listeners

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.features.register.dialogs.VerifyRegistrationCodeDialog
import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.event.player.PlayerCustomClickEvent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

class VerifyCodeDialogListenerTest {
    private val registerService = mock(RegisterHttpService::class.java)
    private val syncPlayer = mock(SyncPlayer::class.java)
    private val listener = VerifyCodeDialogListener(registerService, syncPlayer)
    private val player = mock(Player::class.java)
    private val playerId = UUID.randomUUID()
    private val connection = mock(PlayerGameConnection::class.java)

    init {
        listener.errorDialogFactory = { mock(DialogLike::class.java) }
    }

    @Test
    fun `ignores clicks outside the verification dialog`() =
        runTest {
            val event = mock(PlayerCustomClickEvent::class.java)
            whenever(event.identifier).thenReturn(Key.key("pcbridge:unrelated"))

            listener.onPlayerCustomClickEvent(event)

            verify(registerService, never()).verifyCode(any(), any())
        }

    @Test
    fun `shows a validation dialog when the code is empty`() =
        runTest {
            val event = validEvent("")

            listener.onPlayerCustomClickEvent(event)

            verify(player).showDialog(any())
            verify(registerService, never()).verifyCode(any(), any())
        }

    @Test
    fun `trims and verifies a code then synchronizes the player`() =
        runTest {
            val event = validEvent(" 123456 ")

            listener.onPlayerCustomClickEvent(event)

            verify(registerService).verifyCode(code = "123456", playerUUID = playerId)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.registrationComplete)
            verify(syncPlayer).execute(playerUUID = playerId)
        }

    @Test
    fun `shows an error dialog when the code is invalid or expired`() =
        runTest {
            val event = validEvent("000000")
            whenever(registerService.verifyCode(code = "000000", playerUUID = playerId))
                .thenAnswer { throw ResponseParserError.NotFound(null) }

            listener.onPlayerCustomClickEvent(event)

            verify(player).showDialog(any())
            verify(syncPlayer, never()).execute(any())
        }

    private fun validEvent(code: String): PlayerCustomClickEvent {
        val response = mock(DialogResponseView::class.java)
        val event = mock(PlayerCustomClickEvent::class.java)
        whenever(response.getText(VerifyRegistrationCodeDialog.codeKey)).thenReturn(code)
        whenever(event.identifier).thenReturn(VerifyRegistrationCodeDialog.verifyButtonKey)
        whenever(event.dialogResponseView).thenReturn(response)
        whenever(event.commonConnection).thenReturn(connection)
        whenever(connection.player).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
        return event
    }
}
