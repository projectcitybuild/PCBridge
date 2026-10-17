package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class HubCommandTest {
    private val player = mock(Player::class.java)
    private val server = mock(Server::class.java)
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val playerTeleporter = mock(PlayerTeleporter::class.java)

    @Test
    fun `reports when the hub is not configured`() =
        runTest {
            val source = setUpPlayerSource()
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(version = 1, config = RemoteConfigKeyValues()),
            )
            val command = command(this)

            execute(command, "hub", source)
            advanceUntilIdle()

            verify(player).sendMessage(l10n.errorHubNotSet)
            verify(server, never()).getWorld(org.mockito.kotlin.any<UUID>())
            verify(
                playerTeleporter,
                never(),
            ).move(
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
            )
        }

    @Test
    fun `reports when the configured hub world is unavailable`() =
        runTest {
            val source = setUpPlayerSource()
            val worldId = UUID.randomUUID()
            whenever(remoteConfig.latest).thenReturn(configuredHub(worldId))
            whenever(server.getWorld(worldId)).thenReturn(null)
            val command = command(this)

            execute(command, "hub", source)
            advanceUntilIdle()

            verify(player).sendMessage(l10n.errorHubWorldNotFound)
            verify(playerTeleporter, never()).move(
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any(),
            )
        }

    @Test
    fun `teleports the player to the configured hub`() =
        runTest {
            val source = setUpPlayerSource()
            val worldId = UUID.randomUUID()
            val world = mock(World::class.java)
            whenever(remoteConfig.latest).thenReturn(configuredHub(worldId))
            whenever(server.getWorld(worldId)).thenReturn(world)
            val command = command(this)

            execute(command, "hub", source)
            advanceUntilIdle()

            verify(playerTeleporter).move(
                player,
                Location(world, 10.5, 70.0, -3.5, 90f, 15f),
                PlayerTeleportEvent.TeleportCause.COMMAND,
                PlayerTeleporter.TeleportOptions(),
            )
            verify(player).sendMessage(l10n.teleportedToHub)
        }

    private fun command(scope: CoroutineScope) =
        HubCommand(
            plugin = mock(Plugin::class.java),
            server = server,
            remoteConfig = remoteConfig,
            playerTeleporter = playerTeleporter,
            commandTaskExecutor = CoroutineScopeCommandTaskExecutor(scope),
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

    private fun configuredHub(worldId: UUID) =
        RemoteConfigVersion(
            version = 1,
            config =
                RemoteConfigKeyValues(
                    hub =
                        RemoteConfigKeyValues.Hub(
                            worldId = worldId.toString(),
                            x = 10.5,
                            y = 70.0,
                            z = -3.5,
                            yaw = 90f,
                            pitch = 15f,
                        ),
                ),
        )
}
