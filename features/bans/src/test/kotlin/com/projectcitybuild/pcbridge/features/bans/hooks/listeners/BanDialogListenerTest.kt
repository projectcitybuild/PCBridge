package com.projectcitybuild.pcbridge.features.bans.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.features.bans.domain.actions.CreateUuidBan
import com.projectcitybuild.pcbridge.features.bans.hooks.dialogs.CreateBanDialog
import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.event.player.PlayerCustomClickEvent
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.key.Key
import org.bukkit.Server
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class BanDialogListenerTest {
    private val createUuidBan = mock(CreateUuidBan::class.java)
    private val server = mock(Server::class.java)
    private val listener = BanDialogListener(server, createUuidBan)
    private val player = mock(Player::class.java)
    private val connection = mock(PlayerGameConnection::class.java)

    @Test
    fun `ignores unrelated dialog clicks`() =
        runTest {
            val event = mock(PlayerCustomClickEvent::class.java)
            whenever(event.identifier).thenReturn(Key.key("pcbridge:other"))

            listener.onPlayerCustomClickEvent(event)

            verify(createUuidBan, never()).create(any(), any(), any(), any(), any())
        }

    @Test
    fun `trims submitted fields and creates a ban`() =
        runTest {
            val response = mock(DialogResponseView::class.java)
            val event = mock(PlayerCustomClickEvent::class.java)
            val playerId = UUID.randomUUID()
            val ban =
                PlayerBan(
                    id = 15,
                    reason = "Griefing",
                    createdAt = LocalDateTime.parse("2024-01-01T00:00:00"),
                    updatedAt = LocalDateTime.parse("2024-01-01T00:00:00"),
                )
            val creation = CreateUuidBan.Creation(ban, UUID.randomUUID(), "https://manage/player-bans/15/edit")
            whenever(event.identifier).thenReturn(CreateBanDialog.submitBanButtonKey)
            whenever(event.dialogResponseView).thenReturn(response)
            whenever(event.commonConnection).thenReturn(connection)
            whenever(connection.player).thenReturn(player)
            whenever(player.uniqueId).thenReturn(playerId)
            whenever(player.name).thenReturn("Moderator")
            whenever(response.getText(CreateBanDialog.playerNameKey)).thenReturn(" Steve ")
            whenever(response.getText(CreateBanDialog.reasonKey)).thenReturn(" Griefing ")
            whenever(response.getText(CreateBanDialog.additionalInfoKey)).thenReturn(" Context ")
            whenever(
                createUuidBan.create(
                    bannedAlias = "Steve",
                    bannerUuid = playerId,
                    bannerAlias = "Moderator",
                    reason = "Griefing",
                    additionalInfo = "Context",
                ),
            ).thenReturn(creation)
            whenever(server.onlinePlayers).thenReturn(emptySet())

            listener.onPlayerCustomClickEvent(event)

            verify(createUuidBan).create("Steve", playerId, "Moderator", "Griefing", "Context")
            verify(server).broadcast(l10n.bans.playerHasBeenBanned("Steve"))
            verify(player).sendMessage(l10n.bans.clickToEditBan(creation.editUrl))
        }
}
