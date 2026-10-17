package com.projectcitybuild.pcbridge.features.homes.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Home
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.HomeLimit
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeRenameCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class HomeCommandHandlersTest {
    private val plugin = mock(Plugin::class.java)
    private val repository = mock(HomeRepository::class.java)
    private val player = mock(Player::class.java)
    private val source = mock(CommandSourceStack::class.java)
    private val playerId = UUID.randomUUID()

    init {
        whenever(source.sender).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
        whenever(player.hasPermission(any<String>())).thenReturn(true)
    }

    @Test
    fun `HomeCreateCommand creates a home using the player's location`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val location = playerLocation()
            whenever(repository.create("Mine", location, player)).thenReturn(home(name = "Mine"))

            runCommand(this, homesCommand(executor), "homes create Mine")

            verify(repository).create("Mine", location, player)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.homeCreated("Mine"))
        }

    @Test
    fun `HomeDeleteCommand deletes the requested home for its owner`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)

            runCommand(this, homesCommand(executor), "homes delete Cabin")

            verify(repository).delete(player, "Cabin")
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.homeDeleted("Cabin"))
        }

    @Test
    fun `HomeLimitCommand reports a player with no home allowance`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.limit(player)).thenReturn(HomeLimit(max = 0, current = 0, sources = emptyMap()))

            runCommand(this, homesCommand(executor), "homes limit")

            verify(player).sendRichMessage("<gray>You cannot create a home</gray>")
        }

    @Test
    fun `HomeLimitCommand displays allowance counts and role sources`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.limit(player)).thenReturn(HomeLimit(max = 5, current = 2, sources = mapOf("VIP" to 2)))

            runCommand(this, homesCommand(executor), "homes limit")

            verify(player).sendRichMessage("You currently have <aqua>2</aqua> of <aqua>5</aqua> homes")
            verify(player).sendRichMessage("<gray>---</gray>")
            verify(
                player,
            ).sendRichMessage(
                "<gray>The following roles grant you homes:</gray> <newline><gray><white>VIP</white>: <aqua>+2</aqua> homes</gray>",
            )
        }

    @Test
    fun `HomeListCommand fetches the selected page using remote page size`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val remoteConfig = remoteConfig()
            whenever(repository.all(playerId, 2, 15)).thenReturn(PaginatedList(emptyList(), 2, 15, 0, 0))

            runCommand(this, homesCommand(executor, remoteConfig), "homes list 2")

            verify(repository).all(playerId, 2, 15)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.errorPageNotFound)
        }

    @Test
    fun `HomeMoveCommand moves the named home to the player's current location`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val location = playerLocation()
            whenever(repository.move("Cabin", location, player)).thenReturn(home(name = "Cabin"))

            runCommand(this, homesCommand(executor), "homes move Cabin")

            verify(repository).move("Cabin", location, player)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.homeMoved("Cabin"))
        }

    @Test
    fun `HomeRenameCommand looks up the home before building its rename dialog`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.get(playerId, "Cabin")).thenReturn(home(name = "Cabin"))

            runCommand(this, homesCommand(executor), "homes rename Cabin")

            verify(repository).get(playerId, "Cabin")
        }

    @Test
    fun `HomeCommand teleports to the stored home coordinates`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val server = mock(Server::class.java)
            val world = mock(World::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            whenever(repository.get(playerId, "Cabin")).thenReturn(home(name = "Cabin"))
            whenever(server.getWorld("world")).thenReturn(world)

            runCommand(
                this,
                HomeCommand(plugin, server, mock(), repository, teleporter, executor),
                "home Cabin",
            )

            verify(teleporter).move(
                eq(player),
                eq(Location(world, 10.0, 70.0, -5.0, 45f, 15f)),
                eq(PlayerTeleportEvent.TeleportCause.COMMAND),
                eq(PlayerTeleporter.TeleportOptions()),
            )
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.teleportedToName("Cabin"))
        }

    @Test
    fun `HomeCommand does not teleport when the home world is unavailable`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val server = mock(Server::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            whenever(repository.get(playerId, "Cabin")).thenReturn(home(name = "Cabin"))

            runCommand(
                this,
                HomeCommand(plugin, server, mock(), repository, teleporter, executor),
                "home Cabin",
            )

            verify(teleporter, never()).move(any(), any(), any(), any())
        }

    @Test
    fun `HomesCommand executes its default list handler through the dispatcher`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.all(playerId, 1, 15)).thenReturn(PaginatedList(emptyList(), 1, 15, 0, 0))
            val command = homesCommand(executor)

            assertEquals(
                setOf("create", "delete", "list", "move", "limit", "rename"),
                command.literal().children.map { it.name }.toSet(),
            )
            runCommand(this, command, "homes")

            verify(repository).all(playerId, 1, 15)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.noHomesFound)
        }

    private fun homesCommand(
        executor: CoroutineScopeCommandTaskExecutor,
        remoteConfig: RemoteConfig = remoteConfig(),
    ) = HomesCommand(
        HomeCreateCommand(plugin, repository, executor),
        HomeDeleteCommand(plugin, mock(), repository, executor),
        HomeListCommand(plugin, repository, remoteConfig, executor),
        HomeMoveCommand(plugin, mock(), repository, executor),
        HomeLimitCommand(plugin, repository, executor),
        HomeRenameCommand(plugin, mock(), repository, executor),
    )

    private fun remoteConfig(): RemoteConfig {
        val remoteConfig = mock(RemoteConfig::class.java)
        whenever(remoteConfig.latest).thenReturn(
            com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion(
                1,
                com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues(),
            ),
        )
        return remoteConfig
    }

    private fun runCommand(
        scope: TestScope,
        command: BrigadierCommand,
        input: String,
    ) {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
        scope.testScheduler.advanceUntilIdle()
    }

    private fun playerLocation(): Location {
        val world = mock(World::class.java)
        val location = Location(world, 10.0, 70.0, -5.0, 15f, 45f)
        whenever(world.name).thenReturn("world")
        whenever(player.location).thenReturn(location)
        return location
    }

    private fun home(name: String): Home {
        return Home(
            1,
            name,
            "world",
            10.0,
            70.0,
            -5.0,
            15f,
            45f,
            LocalDateTime.MIN,
            LocalDateTime.MIN,
        )
    }
}
