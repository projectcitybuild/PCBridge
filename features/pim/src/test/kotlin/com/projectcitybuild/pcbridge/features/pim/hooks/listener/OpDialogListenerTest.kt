package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.dialogs.ConfirmOpElevateDialog
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.event.player.PlayerCustomClickEvent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

class OpDialogListenerTest {
    private val service = mock(OpElevationService::class.java)
    private val listener = OpDialogListener(service)
    private val player = mock(Player::class.java)
    private val playerId = UUID.randomUUID()

    @Test
    fun `ignores unrelated dialog clicks`() =
        runTest {
            val event = mock(PlayerCustomClickEvent::class.java)
            whenever(event.identifier).thenReturn(Key.key("pcbridge:other"))

            listener.onPlayerCustomClickEvent(event)

            verify(service, never()).elevate(any(), any())
        }

    @Test
    fun `requires a nonblank reason before elevating`() =
        runTest {
            val event = validEvent("   ")

            listener.onPlayerCustomClickEvent(event)

            verify(player).sendRichMessage("<red>Error: elevation reason is required</red>")
            verify(service, never()).elevate(any(), any())
        }

    @Test
    fun `trims the reason before requesting elevation`() =
        runTest {
            val event = validEvent("  building maintenance ")

            listener.onPlayerCustomClickEvent(event)

            verify(service).elevate(playerId, "building maintenance")
        }

    private fun validEvent(reason: String): PlayerCustomClickEvent {
        val response = mock(DialogResponseView::class.java)
        val event = mock(PlayerCustomClickEvent::class.java)
        val connection = mock(PlayerGameConnection::class.java)
        whenever(response.getText(ConfirmOpElevateDialog.REASON_KEY)).thenReturn(reason)
        whenever(event.identifier).thenReturn(ConfirmOpElevateDialog.proceedButtonKey)
        whenever(event.dialogResponseView).thenReturn(response)
        whenever(event.commonConnection).thenReturn(connection)
        whenever(connection.player).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
        return event
    }
}
