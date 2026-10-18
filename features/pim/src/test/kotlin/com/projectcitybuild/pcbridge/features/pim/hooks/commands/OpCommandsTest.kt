package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.state.data.OpElevation
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runCurrent
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
    private val playerId = UUID.randomUUID()

    @Test
    fun `grant command opens confirmation when player is not elevated`() =
        runTest {
            val player = mock(Player::class.java)
            val elevationService = mock(OpElevationService::class.java)
            whenever(elevationService.elevation(playerId)).thenReturn(null)
            val dialog = mock(DialogLike::class.java)
            val command =
                OpGrantCommand(
                    mock(Plugin::class.java),
                    elevationService,
                    mock(LocalizedTime::class.java),
                    CoroutineScopeCommandTaskExecutor(this),
                )
            command.confirmationDialog = { dialog }

            dispatch(command, "grant", player)

            verify(player).showDialog(dialog)
        }

    @Test
    fun `grant command reports remaining time instead of opening confirmation for active elevation`() =
        runTest {
            val player = mock(Player::class.java)
            val elevationService = mock(OpElevationService::class.java)
            val localizedTime = mock(LocalizedTime::class.java)
            val now = Instant.parse("2025-01-01T00:00:00Z")
            whenever(localizedTime.nowInstant()).thenReturn(now)
            whenever(elevationService.elevation(playerId)).thenReturn(
                OpElevation(1, "test", now.minusSeconds(20), now.plusSeconds(60)),
            )
            val command =
                OpGrantCommand(
                    mock(Plugin::class.java),
                    elevationService,
                    localizedTime,
                    CoroutineScopeCommandTaskExecutor(this),
                )

            dispatch(command, "grant", player)

            verify(player).sendRichMessage(org.mockito.kotlin.argThat { contains("already OP elevated") })
            verify(player, never()).showDialog(any())
        }

    @Test
    fun `revoke command revokes the invoking player`() =
        runTest {
            val player = mock(Player::class.java)
            val elevationService = mock(OpElevationService::class.java)
            val command =
                OpRevokeCommand(
                    mock(Plugin::class.java),
                    elevationService,
                    CoroutineScopeCommandTaskExecutor(this),
                )

            dispatch(command, "revoke", player)

            verify(elevationService).revoke(playerId)
        }

    @Test
    fun `status command reports when the player is not elevated`() =
        runTest {
            val player = mock(Player::class.java)
            val elevationService = mock(OpElevationService::class.java)
            whenever(elevationService.elevation(playerId)).thenReturn(null)
            val command =
                OpStatusCommand(
                    mock(Plugin::class.java),
                    elevationService,
                    mock(LocalizedTime::class.java),
                    CoroutineScopeCommandTaskExecutor(this),
                )

            dispatch(command, "status", player)

            verify(player).sendRichMessage("<gray>You are not currently OP elevated</gray>")
        }

    private suspend fun kotlinx.coroutines.test.TestScope.dispatch(
        command: BrigadierCommand,
        input: String,
        player: Player,
    ) {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
        whenever(player.hasPermission(PermissionNode.PIM_OP.node)).thenReturn(true)
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
        runCurrent()
    }
}
