package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class SpawnCommandTest {
    private val player = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val spawnRepository = mock(SpawnRepository::class.java)
    private val playerTeleporter = mock(PlayerTeleporter::class.java)
    private val command = SpawnCommand(mock(Plugin::class.java), spawnRepository, playerTeleporter)

    @Test
    fun `teleports player to the spawn saved for their current world`() =
        runTest {
            val world = mock(World::class.java)
            val currentLocation = Location(world, 1.0, 64.0, 2.0)
            val spawn = Location(world, 10.0, 70.0, 20.0)
            setUpContext()
            whenever(player.location).thenReturn(currentLocation)
            whenever(player.name).thenReturn("player")
            whenever(spawnRepository.get(world)).thenReturn(spawn)

            command.execute(context)

            verify(spawnRepository).get(world)
            verify(playerTeleporter).move(
                player = player,
                destination = spawn,
                cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            )
            verify(player).sendMessage(l10n.teleportedToSpawn)
        }

    private fun setUpContext() {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/spawn")
    }
}
