package com.projectcitybuild.pcbridge.features.randomteleport.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.randomteleport.domain.actions.FindRandomLocation
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.cooldowns.Cooldown
import io.papermc.paper.command.brigadier.CommandSourceStack
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
    private val context = mock(PaperCommandContext::class.java)
    private val cooldown = mock(Cooldown::class.java)
    private val locationFinder = mock(FindRandomLocation::class.java)
    private val command = RtpCommand(mock(Plugin::class.java), cooldown, locationFinder)

    @Test
    fun `reports failure after searching when no safe location is found`() =
        runTest {
            setUpPlayerContext()
            whenever(locationFinder.teleport(player, attempts = 5)).thenReturn(null)

            command.execute(context)

            verify(cooldown).throttle(5.seconds, player, "rtp")
            verify(locationFinder).teleport(player, attempts = 5)
            verify(player).sendMessage(l10n.searchingForSafeLocation)
            verify(player).sendMessage(l10n.errorCouldNotFindSafeLocation)
        }

    @Test
    fun `reports destination coordinates after successful teleport`() =
        runTest {
            setUpPlayerContext()
            whenever(locationFinder.teleport(player, attempts = 5))
                .thenReturn(Location(null, 12.9, 65.8, -4.2))

            command.execute(context)

            verify(player).sendMessage(l10n.teleportedToCoordinate(12, 65, -4))
        }

    private fun setUpPlayerContext() {
        val source = mock(CommandSourceStack::class.java)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/rtp")
        whenever(source.sender).thenReturn(player)
        whenever(player.name).thenReturn("player")
    }
}
