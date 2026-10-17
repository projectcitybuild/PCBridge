package com.projectcitybuild.pcbridge.features.randomteleport.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.randomteleport.domain.actions.FindRandomLocation
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.cooldowns.Cooldown
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.seconds

class RtpCommandTest {
    private val player = mock(Player::class.java)
    private val cooldown = mock(Cooldown::class.java)
    private val locationFinder = mock(FindRandomLocation::class.java)

    @Test
    fun `reports failure after searching when no safe location is found`() =
        runTest {
            val source = setUpPlayerSource()
            whenever(locationFinder.teleport(player, attempts = 5)).thenReturn(null)
            val command = command(this)

            execute(command, "rtp", source)
            advanceUntilIdle()

            verify(cooldown).throttle(5.seconds, player, "rtp")
            verify(locationFinder).teleport(player, attempts = 5)
            verify(player).sendMessage(l10n.searchingForSafeLocation)
            verify(player).sendMessage(l10n.errorCouldNotFindSafeLocation)
        }

    @Test
    fun `reports destination coordinates after successful teleport`() =
        runTest {
            val source = setUpPlayerSource()
            whenever(locationFinder.teleport(player, attempts = 5))
                .thenReturn(Location(null, 12.9, 65.8, -4.2))
            val command = command(this)

            execute(command, "rtp", source)
            advanceUntilIdle()

            verify(player).sendMessage(l10n.teleportedToCoordinate(12, 65, -4))
        }

    private fun command(scope: CoroutineScope) =
        RtpCommand(
            mock(Plugin::class.java),
            cooldown,
            locationFinder,
            CoroutineScopeCommandTaskExecutor(scope),
        )

    private fun setUpPlayerSource(): CommandSourceStack {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(PermissionNode.TELEPORT_RANDOM.node))
            .thenReturn(true)
        whenever(player.name).thenReturn("player")
        return source
    }

    private fun execute(
        command: BrigadierCommand,
        input: String,
        source: CommandSourceStack,
    ) {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
    }
}
