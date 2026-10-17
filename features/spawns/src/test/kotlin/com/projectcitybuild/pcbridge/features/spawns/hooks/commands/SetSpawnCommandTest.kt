package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.spawns.domain.data.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.util.UUID

class SetSpawnCommandTest {
    private val player = mock(Player::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val spawnRepository = mock(SpawnRepository::class.java)
    private val eventBroadcaster = mock(SpigotEventBroadcaster::class.java)
    private val command = SetSpawnCommand(mock(Plugin::class.java), spawnRepository, eventBroadcaster)

    @Test
    fun `saves the player's current location and broadcasts the spawn update`() =
        runTest {
            val world = mock(World::class.java)
            val worldId = UUID.randomUUID()
            val location = Location(world, 10.0, 64.0, -5.0)
            whenever(world.uid).thenReturn(worldId)
            setUpContext()
            whenever(player.location).thenReturn(location)
            whenever(player.name).thenReturn("player")

            command.execute(context)

            verify(spawnRepository).set(location)
            val eventCaptor = argumentCaptor<SpawnUpdatedEvent>()
            verify(eventBroadcaster).broadcast(eventCaptor.capture())
            org.junit.jupiter.api.Assertions.assertEquals(worldId, eventCaptor.firstValue.worldId)
            org.junit.jupiter.api.Assertions.assertEquals(location, eventCaptor.firstValue.location)
            verify(player).sendMessage(l10n.spawnSet(location))
        }

    private fun setUpContext() {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/setspawn")
    }
}
