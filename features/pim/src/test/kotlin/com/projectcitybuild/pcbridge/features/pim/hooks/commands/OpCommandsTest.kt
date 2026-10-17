package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.state.data.OpElevation
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpCommandsTest {
    private val player = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val elevationService = mock(OpElevationService::class.java)
    private val localizedTime = mock(LocalizedTime::class.java)
    private val playerId = UUID.randomUUID()

    @Test
    fun `grant command opens confirmation when player is not elevated`() =
        runTest {
            setUpContext()
            whenever(elevationService.elevation(playerId)).thenReturn(null)
            val dialog = mock(DialogLike::class.java)
            val command = OpGrantCommand(mock(Plugin::class.java), elevationService, localizedTime)
            command.confirmationDialog = { dialog }

            command.execute(context)

            verify(player).showDialog(dialog)
        }

    @Test
    fun `grant command reports remaining time instead of opening confirmation for active elevation`() =
        runTest {
            setUpContext()
            val now = Instant.parse("2025-01-01T00:00:00Z")
            whenever(localizedTime.nowInstant()).thenReturn(now)
            whenever(elevationService.elevation(playerId)).thenReturn(
                OpElevation(1, "test", now.minusSeconds(20), now.plusSeconds(60)),
            )

            OpGrantCommand(mock(Plugin::class.java), elevationService, localizedTime).execute(context)

            verify(player).sendRichMessage(org.mockito.kotlin.argThat { contains("already OP elevated") })
            verify(player, never()).showDialog(any())
        }

    @Test
    fun `revoke command revokes the invoking player`() =
        runTest {
            setUpContext()

            OpRevokeCommand(mock(Plugin::class.java), elevationService).execute(context)

            verify(elevationService).revoke(playerId)
        }

    @Test
    fun `status command reports when the player is not elevated`() =
        runTest {
            setUpContext()
            whenever(elevationService.elevation(playerId)).thenReturn(null)

            OpStatusCommand(mock(Plugin::class.java), elevationService, localizedTime).execute(context)

            verify(player).sendRichMessage("<gray>You are not currently OP elevated</gray>")
        }

    private fun setUpContext() {
        val source = mock(CommandSourceStack::class.java)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/pim op")
        whenever(source.sender).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
    }
}
