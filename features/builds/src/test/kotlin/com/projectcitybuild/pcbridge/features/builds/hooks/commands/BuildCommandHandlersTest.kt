package com.projectcitybuild.pcbridge.features.builds.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Build
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.features.builds.domain.data.EditableBuildField
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildDeleteCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildEditCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildMoveCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildSetCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildUnvoteCommand
import com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds.BuildVoteCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Player as BuildOwner

class BuildCommandHandlersTest {
    private val plugin = mock(Plugin::class.java)
    private val repository = mock(BuildRepository::class.java)
    private val player = mock(Player::class.java)
    private val source = mock(CommandSourceStack::class.java)

    init {
        whenever(source.sender).thenReturn(player)
        whenever(player.hasPermission(any<String>())).thenReturn(true)
    }

    @Test
    fun `BuildCreateCommand stores the player's current location and broadcasts creation`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val world = mock(World::class.java)
            val location = Location(world, 1.5, 64.0, -2.0, 20f, 30f)
            val server = mock(Server::class.java)
            whenever(plugin.server).thenReturn(server)
            whenever(player.name).thenReturn("builder")
            whenever(player.location).thenReturn(location)
            whenever(world.name).thenReturn("world")
            whenever(repository.create("Sky Base", "world", location, player)).thenReturn(build(name = "Sky Base"))

            runCommand(this, buildsCommand(executor), "builds create Sky Base")

            verify(repository).create("Sky Base", "world", location, player)
            verify(player).sendMessage(l10n.builds.buildCreated("Sky Base"))
            verify(server).broadcast(any<Component>())
        }

    @Test
    fun `BuildDeleteCommand deletes the named build for the invoking player`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)

            runCommand(this, buildsCommand(executor), "builds delete Old Base")

            verify(repository).delete("Old Base", player)
            verify(player).sendMessage(l10n.builds.buildDeleted("Old Base"))
        }

    @Test
    fun `BuildEditCommand rejects a build that cannot be found`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.get("Missing")).thenReturn(null)

            runCommand(this, buildsCommand(executor), "builds edit Missing")

            verify(repository).get("Missing")
            verify(player).sendMessage(l10n.errors.commandErrorWithPrefix("Build not found"))
        }

    @Test
    fun `BuildEditCommand presents editable fields for a found build`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.get("Main Base")).thenReturn(build(name = "Main Base"))

            runCommand(this, buildsCommand(executor), "builds edit Main Base")

            verify(player).sendMessage(any<Component>())
        }

    @Test
    fun `BuildListCommand fetches the requested page and reports an empty result`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.all(3)).thenReturn(PaginatedList(emptyList(), 3, 10, 0, 0))

            runCommand(this, buildsCommand(executor), "builds list 3")

            verify(repository).all(3)
            verify(player).sendMessage(l10n.pagination.errorPageNotFound)
        }

    @Test
    fun `BuildMoveCommand updates the build to the player's location`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val world = mock(World::class.java)
            val location = Location(world, 1.5, 64.0, -2.0, 20f, 30f)
            whenever(player.location).thenReturn(location)
            whenever(world.name).thenReturn("world")
            whenever(repository.update("Main Base", "world", location, player)).thenReturn(build(name = "Main Base"))

            runCommand(this, buildsCommand(executor), "builds move Main Base")

            verify(repository).update("Main Base", "world", location, player)
            verify(player).sendMessage(l10n.builds.buildMoved("Main Base"))
        }

    @Test
    fun `BuildSetCommand forwards the selected field and value`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)

            runCommand(this, buildsCommand(executor), "builds set 42 LORE A detailed legend")

            verify(repository).set(42, player, EditableBuildField.LORE, "A detailed legend")
            verify(player).sendMessage(l10n.builds.buildUpdated)
        }

    @Test
    fun `BuildUnvoteCommand removes the invoking player's vote`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.unvote("Main Base", player)).thenReturn(build(name = "Main Base"))

            runCommand(this, buildsCommand(executor), "builds unvote Main Base")

            verify(repository).unvote("Main Base", player)
            verify(player).sendMessage(l10n.builds.buildVoteRemoved("Main Base"))
        }

    @Test
    fun `BuildVoteCommand votes and announces the build`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val server = mock(Server::class.java)
            whenever(plugin.server).thenReturn(server)
            whenever(player.name).thenReturn("builder")
            whenever(repository.vote("Main Base", player)).thenReturn(build(name = "Main Base"))

            runCommand(this, buildsCommand(executor), "builds vote Main Base")

            verify(repository).vote("Main Base", player)
            verify(player).sendMessage(l10n.builds.buildVoteRecorded("Main Base"))
            verify(server).broadcast(any<Component>())
        }

    @Test
    fun `BuildCommand teleports the player to the stored build location`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            val world = mock(World::class.java)
            val server = mock(Server::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            whenever(repository.get("Main Base")).thenReturn(build(name = "Main Base"))
            whenever(server.getWorld("world")).thenReturn(world)

            runCommand(this, BuildCommand(plugin, mock(), repository, server, teleporter, executor), "build Main Base")

            verify(teleporter).move(
                eq(player),
                eq(Location(world, 1.5, 64.0, -2.0, 30f, 20f)),
                eq(PlayerTeleportEvent.TeleportCause.COMMAND),
                eq(PlayerTeleporter.TeleportOptions(preloadDestinationChunk = true)),
            )
            verify(player).showTitle(any<Title>())
            verify(player).sendMessage(l10n.builds.buildTeleportedTo("Main Base"))
        }

    @Test
    fun `BuildsCommand executes its default list handler through the dispatcher`() =
        runTest {
            val executor = CoroutineScopeCommandTaskExecutor(this)
            whenever(repository.all(1)).thenReturn(PaginatedList(emptyList(), 1, 10, 0, 0))

            val command = buildsCommand(executor)
            assertEquals(
                setOf("create", "delete", "edit", "list", "move", "set", "unvote", "vote"),
                command.literal().children.map { it.name }.toSet(),
            )
            runCommand(this, command, "builds")

            verify(repository).all(1)
            verify(player).sendMessage(l10n.builds.errorNoBuildsFound)
        }

    private fun buildsCommand(executor: CoroutineScopeCommandTaskExecutor) =
        BuildsCommand(
            BuildCreateCommand(plugin, repository, executor),
            BuildDeleteCommand(plugin, mock(), repository, executor),
            BuildEditCommand(plugin, mock(), repository, executor),
            BuildListCommand(plugin, repository, executor),
            BuildMoveCommand(plugin, mock(), repository, executor),
            BuildSetCommand(plugin, repository, executor),
            BuildUnvoteCommand(plugin, mock(), repository, executor),
            BuildVoteCommand(plugin, mock(), repository, executor),
        )

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

    private fun build(name: String) =
        Build(
            id = 42,
            name = name,
            description = "A description",
            lore = "A legend",
            votes = 3,
            world = "world",
            x = 1.5,
            y = 64.0,
            z = -2.0,
            pitch = 20f,
            yaw = 30f,
            createdAt = LocalDateTime.MIN,
            updatedAt = LocalDateTime.MIN,
            player = BuildOwner(1, UUID.randomUUID().toString(), "builder"),
        )
}
