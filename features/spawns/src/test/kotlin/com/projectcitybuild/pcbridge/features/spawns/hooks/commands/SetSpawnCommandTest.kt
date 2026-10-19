package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.spawns.SpawnsPermissions
import com.projectcitybuild.pcbridge.features.spawns.domain.data.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
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

class SetSpawnCommandTest {
    private val player = mock(Player::class.java)
    private val spawnRepository = mock(SpawnRepository::class.java)
    private val eventBroadcaster = mock(SpigotEventBroadcaster::class.java)

    @Test
    fun `saves the player's current location and broadcasts the spawn update`() =
        runTest {
            val source = setUpPlayerSource()
            val world = mock(World::class.java)
            val worldId = UUID.randomUUID()
            val location = Location(world, 10.0, 64.0, -5.0)
            whenever(world.uid).thenReturn(worldId)
            whenever(player.location).thenReturn(location)
            whenever(player.name).thenReturn("player")
            val command = command(this)

            execute(command, "setspawn", source)
            advanceUntilIdle()

            verify(spawnRepository).set(location)
            val eventCaptor = argumentCaptor<SpawnUpdatedEvent>()
            verify(eventBroadcaster).broadcast(eventCaptor.capture())
            org.junit.jupiter.api.Assertions.assertEquals(worldId, eventCaptor.firstValue.worldId)
            org.junit.jupiter.api.Assertions.assertEquals(location, eventCaptor.firstValue.location)
            verify(player).sendMessage(l10n.spawns.spawnSet(location))
        }

    private fun command(scope: CoroutineScope) =
        SetSpawnCommand(
            mock(Plugin::class.java),
            spawnRepository,
            eventBroadcaster,
            CoroutineScopeCommandTaskExecutor(scope),
        )

    private fun setUpPlayerSource(): CommandSourceStack {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(SpawnsPermissions.MANAGE.node)).thenReturn(true)
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
