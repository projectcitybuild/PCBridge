package com.projectcitybuild.pcbridge.features.homes.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.hooks.dialogs.HomeRenameDialog
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.event.player.PlayerCustomClickEvent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class HomeRenameDialogListenerTest {
    private val repository = mock(HomeRepository::class.java)
    private val listener = HomeRenameDialogListener(repository)
    private val player = mock(Player::class.java)

    @Test
    fun `ignores custom clicks not sent by the home rename dialog`() = runTest {
        val event = mock(PlayerCustomClickEvent::class.java)
        whenever(event.identifier).thenReturn(Key.key("other:button"))

        listener.onPlayerCustomClickEvent(event)

        verify(repository, never()).rename(any(), any(), any())
    }

    @Test
    fun `ignores a matching button with a missing response view`() = runTest {
        val event = eventWithResponse(null)

        listener.onPlayerCustomClickEvent(event)

        verify(repository, never()).rename(any(), any(), any())
    }

    @Test
    fun `trims and saves a valid home name`() = runTest {
        val view = mock(DialogResponseView::class.java)
        whenever(view.getText(HomeRenameDialog.idKey)).thenReturn("27")
        whenever(view.getText(HomeRenameDialog.newNameKey)).thenReturn("  Workshop  ")
        val event = eventWithResponse(view)

        listener.onPlayerCustomClickEvent(event)

        verify(repository).rename(27, "Workshop", player)
        verify(player).sendMessage(l10n.homes.homeRenamed("Workshop"))
    }

    @Test
    fun `does not rename a home when its submitted name is blank`() = runTest {
        val view = mock(DialogResponseView::class.java)
        whenever(view.getText(HomeRenameDialog.idKey)).thenReturn("27")
        whenever(view.getText(HomeRenameDialog.newNameKey)).thenReturn("   ")
        val event = eventWithResponse(view)

        listener.onPlayerCustomClickEvent(event)

        verify(repository, never()).rename(any(), any(), any())
        verify(player).sendMessage(l10n.errors.errorNewNameCannotBeEmpty)
    }

    @Test
    fun `reports a missing home identifier and propagates the failure`() = runTest {
        val view = mock(DialogResponseView::class.java)
        whenever(view.getText(HomeRenameDialog.idKey)).thenReturn(null)
        whenever(view.getText(HomeRenameDialog.newNameKey)).thenReturn("Workshop")
        val event = eventWithResponse(view)
        var thrown: Exception? = null

        try {
            listener.onPlayerCustomClickEvent(event)
        } catch (failure: Exception) {
            thrown = failure
        } finally {
            verify(repository, never()).rename(any(), any(), any())
            verify(player).sendMessage(l10n.homes.errorRenameHomeNotFound)
        }

        assertEquals("Rename failed: home id was null", thrown?.message)
    }

    @Test
    fun `ignores a valid dialog response without an in-game player connection`() = runTest {
        val view = mock(DialogResponseView::class.java)
        whenever(view.getText(HomeRenameDialog.idKey)).thenReturn("27")
        whenever(view.getText(HomeRenameDialog.newNameKey)).thenReturn("Workshop")
        val event = mock(PlayerCustomClickEvent::class.java)
        whenever(event.identifier).thenReturn(HomeRenameDialog.saveButtonKey)
        whenever(event.dialogResponseView).thenReturn(view)

        listener.onPlayerCustomClickEvent(event)

        verify(repository, never()).rename(any(), any(), any())
    }

    private fun eventWithResponse(view: DialogResponseView?): PlayerCustomClickEvent {
        val connection = mock(PlayerGameConnection::class.java)
        whenever(connection.player).thenReturn(player)
        val event = mock(PlayerCustomClickEvent::class.java)
        whenever(event.identifier).thenReturn(HomeRenameDialog.saveButtonKey)
        whenever(event.dialogResponseView).thenReturn(view)
        whenever(event.commonConnection).thenReturn(connection)
        return event
    }
}
