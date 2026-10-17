package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
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
    private val context = mock(PaperCommandContext::class.java)
    private val server = mock(Server::class.java)
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val playerTeleporter = mock(PlayerTeleporter::class.java)
    private val command =
        HubCommand(
            plugin = mock(Plugin::class.java),
            server = server,
            remoteConfig = remoteConfig,
            playerTeleporter = playerTeleporter,
        )

    @Test
    fun `reports when the hub is not configured`() =
        runTest {
            setUpContext()
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(version = 1, config = RemoteConfigKeyValues()),
            )

            command.execute(context)

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
            setUpContext()
            val worldId = UUID.randomUUID()
            whenever(remoteConfig.latest).thenReturn(configuredHub(worldId))
            whenever(server.getWorld(worldId)).thenReturn(null)

            command.execute(context)

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
            setUpContext()
            val worldId = UUID.randomUUID()
            val world = mock(World::class.java)
            whenever(remoteConfig.latest).thenReturn(configuredHub(worldId))
            whenever(server.getWorld(worldId)).thenReturn(world)

            command.execute(context)

            verify(playerTeleporter).move(
                player,
                Location(world, 10.5, 70.0, -3.5, 90f, 15f),
                PlayerTeleportEvent.TeleportCause.COMMAND,
                PlayerTeleporter.TeleportOptions(),
            )
            verify(player).sendMessage(l10n.teleportedToHub)
        }

    private fun setUpContext() {
        val source = mock(CommandSourceStack::class.java)
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/hub")
        whenever(source.sender).thenReturn(player)
        whenever(player.name).thenReturn("player")
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
