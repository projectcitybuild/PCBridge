package com.projectcitybuild.pcbridge.features.warps.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.NamedResource
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.hooks.dialogs.WarpRenameDialog
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.event.player.PlayerCustomClickEvent
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class WarpRenameDialogListenerTest {
    private val repository = mock(WarpRepository::class.java)
    private val listener = WarpRenameDialogListener(repository)
    private val player = mock(Player::class.java)

    @Test
    fun `ignores custom clicks for another dialog action`() =
        runTest {
            val event = mock(PlayerCustomClickEvent::class.java)
            whenever(event.identifier).thenReturn(net.kyori.adventure.key.Key.key("other:button"))

            listener.onPlayerCustomClickEvent(event)

            verify(repository, never()).rename(any(), any())
        }

    @Test
    fun `ignores a matching action when its response view is absent`() =
        runTest {
            val event = eventWithResponse(null)

            listener.onPlayerCustomClickEvent(event)

            verify(repository, never()).rename(any(), any())
        }

    @Test
    fun `trims and saves a valid warp name`() =
        runTest {
            val view = mock(DialogResponseView::class.java)
            whenever(view.getText(WarpRenameDialog.idKey)).thenReturn("11")
            whenever(view.getText(WarpRenameDialog.newNameKey)).thenReturn("  Market  ")
            val event = eventWithResponse(view)

            listener.onPlayerCustomClickEvent(event)

            verify(repository).rename(11, "Market")
            verify(player).sendMessage(l10n.warps.warpRenamed("Market"))
        }

    @Test
    fun `does not rename a warp when the submitted name is blank`() =
        runTest {
            val view = mock(DialogResponseView::class.java)
            whenever(view.getText(WarpRenameDialog.idKey)).thenReturn("11")
            whenever(view.getText(WarpRenameDialog.newNameKey)).thenReturn("   ")
            val event = eventWithResponse(view)

            listener.onPlayerCustomClickEvent(event)

            verify(repository, never()).rename(any(), any())
            verify(player).sendMessage(l10n.errors.errorNewNameCannotBeEmpty)
        }

    @Test
    fun `reports a missing warp identifier and skips the repository`() =
        runTest {
            val view = mock(DialogResponseView::class.java)
            whenever(view.getText(WarpRenameDialog.idKey)).thenReturn(null)
            whenever(view.getText(WarpRenameDialog.newNameKey)).thenReturn("Market")
            val event = eventWithResponse(view)

            listener.onPlayerCustomClickEvent(event)

            verify(repository, never()).rename(any(), any())
            verify(player).sendMessage(l10n.warps.errorRenameWarpNotFound)
        }

    private fun eventWithResponse(view: DialogResponseView?): PlayerCustomClickEvent {
        val connection = mock(PlayerGameConnection::class.java)
        whenever(connection.player).thenReturn(player)
        val event = mock(PlayerCustomClickEvent::class.java)
        whenever(event.identifier).thenReturn(WarpRenameDialog.saveButtonKey)
        whenever(event.dialogResponseView).thenReturn(view)
        whenever(event.commonConnection).thenReturn(connection)
        return event
    }
}

class WarpWebhookListenerTest {
    private val repository = mock(WarpRepository::class.java)
    private val listener = WarpWebhookListener(repository)

    @Test
    fun `replaces cached warp names after a warp sync webhook`() {
        val names = listOf(NamedResource(1, "Market"), NamedResource(2, "Spawn"))

        listener.onWarpsUpdated(WebhookReceivedEvent(WebhookEvent.SyncWarps(names)))

        verify(repository).setNames(names)
    }

    @Test
    fun `ignores webhook events unrelated to warp synchronization`() {
        listener.onWarpsUpdated(
            WebhookReceivedEvent(
                WebhookEvent.SyncRemoteConfig(
                    com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion(
                        1,
                        com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues(),
                    ),
                ),
            ),
        )

        verify(repository, never()).setNames(any())
    }
}
