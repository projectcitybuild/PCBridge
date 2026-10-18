package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val spawnRepository = mock(SpawnRepository::class.java)
    private val playerTeleporter = mock(PlayerTeleporter::class.java)

    @Test
    fun `teleports player to the spawn saved for their current world`() =
        runTest {
            val source = setUpPlayerSource()
            val world = mock(World::class.java)
            val currentLocation = Location(world, 1.0, 64.0, 2.0)
            val spawn = Location(world, 10.0, 70.0, 20.0)
            whenever(player.location).thenReturn(currentLocation)
            whenever(player.name).thenReturn("player")
            whenever(spawnRepository.get(world)).thenReturn(spawn)
            val command = command(this)

            execute(command, "spawn", source)
            advanceUntilIdle()

            verify(spawnRepository).get(world)
            verify(playerTeleporter).move(
                player = player,
                destination = spawn,
                cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            )
            verify(player).sendMessage(l10n.teleportedToSpawn)
        }

    private fun command(scope: CoroutineScope) =
        SpawnCommand(
            mock(Plugin::class.java),
            spawnRepository,
            playerTeleporter,
            CoroutineScopeCommandTaskExecutor(scope),
        )

    private fun setUpPlayerSource(): CommandSourceStack {
        val source = mock(CommandSourceStack::class.java)
        whenever(source.sender).thenReturn(player)
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
