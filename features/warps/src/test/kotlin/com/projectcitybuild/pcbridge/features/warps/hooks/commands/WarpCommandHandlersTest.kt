package com.projectcitybuild.pcbridge.features.warps.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Warp
import com.projectcitybuild.pcbridge.features.warps.domain.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.features.warps.domain.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
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

class WarpCommandHandlersTest {
    private val plugin = mock(Plugin::class.java)
    private val repository = mock(WarpRepository::class.java)
    private val player = mock(Player::class.java)
    private val source = mock(CommandSourceStack::class.java)

    init {
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(any<String>())).thenReturn(true)
    }

    @Test
    fun `WarpCreateCommand creates a warp and broadcasts the create event`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val location = playerLocation()
            whenever(repository.create("Market", location)).thenReturn(warp(name = "Market"))
            val broadcaster = mock(SpigotEventBroadcaster::class.java)

            runCommand(this, warpsCommand(executor, broadcaster = broadcaster), "warps create Market")

            verify(repository).create("Market", location)
            verify(broadcaster).broadcast(any<WarpCreateEvent>())
            verify(player).sendMessage(l10n.warps.warpCreated("Market"))
        }

    @Test
    fun `WarpDeleteCommand deletes the warp and broadcasts the delete event`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val broadcaster = mock(SpigotEventBroadcaster::class.java)

            runCommand(this, warpsCommand(executor, broadcaster = broadcaster), "warps delete Market")

            verify(repository).delete("Market")
            verify(broadcaster).broadcast(any<WarpDeleteEvent>())
            verify(player).sendMessage(l10n.warps.warpDeleted("Market"))
        }

    @Test
    fun `WarpListCommand fetches the configured page and reports a missing page`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.all(4, 15)).thenReturn(PaginatedList(emptyList(), 4, 15, 0, 0))

            runCommand(this, warpsCommand(executor), "warps list 4")

            verify(repository).all(4, 15)
            verify(player).sendMessage(l10n.pagination.errorPageNotFound)
        }

    @Test
    fun `WarpMoveCommand updates the named warp to the player's current location`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val location = playerLocation()

            runCommand(this, warpsCommand(executor), "warps move Market")

            verify(repository).move("Market", location)
            verify(player).sendMessage(l10n.warps.warpMoved("Market"))
        }

    @Test
    fun `WarpRenameCommand looks up the warp before building its rename dialog`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.get("Market")).thenReturn(warp(name = "Market"))
            val renameCommand = WarpRenameCommand(plugin, WarpNameSuggester(repository), repository, executor)
            var dialogBuiltFor: Pair<Int, String>? = null
            renameCommand.showRenameDialog = { _, warpId, warpName ->
                dialogBuiltFor = warpId to warpName
            }

            runCommand(
                this,
                warpsCommand(executor, renameCommand = renameCommand),
                "warps rename Market",
            )

            verify(repository).get("Market")
            assertEquals(17 to "Market", dialogBuiltFor)
        }

    @Test
    fun `WarpCommand teleports the player to the warp coordinates`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val server = mock(Server::class.java)
            val world = mock(World::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            whenever(repository.get("Market")).thenReturn(warp(name = "Market"))
            whenever(server.getWorld("world")).thenReturn(world)

            runCommand(
                this,
                WarpCommand(plugin, mock(), repository, server, teleporter, executor),
                "warp Market",
            )

            verify(teleporter).move(
                eq(player),
                eq(Location(world, 10.0, 70.0, -5.0, 45f, 15f)),
                eq(PlayerTeleportEvent.TeleportCause.COMMAND),
                eq(PlayerTeleporter.TeleportOptions()),
            )
            verify(player).sendMessage(l10n.teleportation.teleportedToName("Market"))
        }

    @Test
    fun `WarpCommand does not teleport when the destination world is missing`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val server = mock(Server::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            whenever(repository.get("Market")).thenReturn(warp(name = "Market"))

            runCommand(
                this,
                WarpCommand(plugin, mock(), repository, server, teleporter, executor),
                "warp Market",
            )

            verify(teleporter, never()).move(any(), any(), any(), any())
        }

    @Test
    fun `WarpsCommand executes its default list handler through the dispatcher`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.all(1, 15)).thenReturn(PaginatedList(emptyList(), 1, 15, 0, 0))
            val command = warpsCommand(executor)

            assertEquals(
                setOf("create", "delete", "list", "move", "rename"),
                command.literal().children.map { it.name }.toSet(),
            )
            runCommand(this, command, "warps")

            verify(repository).all(1, 15)
            verify(player).sendMessage(l10n.warps.noWarpsFound)
        }

    private fun warpsCommand(
        executor: CoroutineScopeCommandTaskExecutor,
        broadcaster: SpigotEventBroadcaster = mock(),
        remoteConfig: RemoteConfig = remoteConfig(),
        renameCommand: WarpRenameCommand =
            WarpRenameCommand(plugin, WarpNameSuggester(repository), repository, executor),
    ) = WarpsCommand(
        WarpCreateCommand(plugin, repository, broadcaster, executor),
        WarpDeleteCommand(plugin, WarpNameSuggester(repository), repository, broadcaster, executor),
        WarpListCommand(plugin, repository, remoteConfig, executor),
        WarpMoveCommand(plugin, WarpNameSuggester(repository), repository, executor),
        renameCommand,
    )

    private fun remoteConfig(): RemoteConfig {
        val remoteConfig = mock(RemoteConfig::class.java)
        whenever(remoteConfig.latest).thenReturn(RemoteConfigVersion(1, RemoteConfigKeyValues()))
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

    private fun warp(name: String) = Warp(17, name, "world", 10.0, 70.0, -5.0, 15f, 45f)
}
