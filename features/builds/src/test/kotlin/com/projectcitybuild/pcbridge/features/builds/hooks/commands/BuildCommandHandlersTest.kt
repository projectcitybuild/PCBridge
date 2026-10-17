package com.projectcitybuild.pcbridge.features.builds.hooks.commands

import com.mojang.brigadier.context.CommandContext
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
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.CommandSourceStack
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
import org.mockito.Mockito.never
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
    private val context = mock(PaperCommandContext::class.java)

    init {
        whenever(context.input).thenReturn("/builds")
    }

    @Test
    fun `BuildCreateCommand stores the player's current location and broadcasts creation`() =
        runTest {
            val world = mock(World::class.java)
            val location = Location(world, 1.5, 64.0, -2.0, 20f, 30f)
            val server = mock(Server::class.java)
            whenever(plugin.server).thenReturn(server)
            whenever(source.sender).thenReturn(player)
            whenever(player.name).thenReturn("builder")
            whenever(player.location).thenReturn(location)
            whenever(world.name).thenReturn("world")
            whenever(context.source).thenReturn(source)
            whenever(context.input).thenReturn("/build Sky Base")
            whenever(context.getArgument("name", String::class.java)).thenReturn("Sky Base")
            whenever(repository.create("Sky Base", "world", location, player)).thenReturn(build(name = "Sky Base"))

            invokeHandler(BuildCreateCommand(plugin, repository), context)

            verify(repository).create("Sky Base", "world", location, player)
            verify(player).sendRichMessage("<green>Sky Base created</green>")
            verify(server).broadcast(any<Component>())
        }

    @Test
    fun `BuildDeleteCommand deletes the named build for the invoking player`() =
        runTest {
            setContext("Old Base")

            invokeHandler(
                BuildDeleteCommand(plugin, mock(), repository),
                context,
            )

            verify(repository).delete("Old Base", player)
            verify(player).sendRichMessage("<green>Old Base deleted</green>")
        }

    @Test
    fun `BuildEditCommand rejects a build that cannot be found`() =
        runTest {
            setContext("Missing")
            whenever(repository.get("Missing")).thenReturn(null)

            invokeHandler(BuildEditCommand(plugin, mock(), repository), context)

            verify(repository).get("Missing")
            verify(player, never()).sendMessage(any<Component>())
        }

    @Test
    fun `BuildEditCommand presents editable fields for a found build`() =
        runTest {
            setContext("Main Base")
            whenever(repository.get("Main Base")).thenReturn(build(name = "Main Base"))

            invokeHandler(BuildEditCommand(plugin, mock(), repository), context)

            verify(player).sendMessage(any<Component>())
        }

    @Test
    fun `BuildListCommand fetches the requested page and reports an empty result`() =
        runTest {
            whenever(context.source).thenReturn(source)
            whenever(source.sender).thenReturn(player)
            whenever(context.getArgument("page", Int::class.java)).thenReturn(3)
            whenever(repository.all(3)).thenReturn(PaginatedList(emptyList(), 3, 10, 0, 0))

            invokeHandler(BuildListCommand(plugin, repository), context)

            verify(repository).all(3)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.errorPageNotFound)
        }

    @Test
    fun `BuildMoveCommand updates the build to the player's location`() =
        runTest {
            val location = setupPlayerLocation()
            whenever(context.getArgument("name", String::class.java)).thenReturn("Main Base")
            whenever(repository.update("Main Base", "world", location, player)).thenReturn(build(name = "Main Base"))

            invokeHandler(BuildMoveCommand(plugin, mock(), repository), context)

            verify(repository).update("Main Base", "world", location, player)
            verify(player).sendRichMessage("<green>Main Base moved to your location</green>")
        }

    @Test
    fun `BuildSetCommand forwards the selected field and value`() =
        runTest {
            setContext("unused")
            whenever(context.getArgument("id", Int::class.java)).thenReturn(42)
            whenever(context.getArgument("field", EditableBuildField::class.java)).thenReturn(EditableBuildField.LORE)
            whenever(context.getArgument("value", String::class.java)).thenReturn("A detailed legend")

            invokeHandler(BuildSetCommand(plugin, repository), context)

            verify(repository).set(42, player, EditableBuildField.LORE, "A detailed legend")
            verify(player).sendRichMessage("<green>Build updated</green>")
        }

    @Test
    fun `BuildUnvoteCommand removes the invoking player's vote`() =
        runTest {
            setContext("Main Base")
            whenever(repository.unvote("Main Base", player)).thenReturn(build(name = "Main Base"))

            invokeHandler(BuildUnvoteCommand(plugin, mock(), repository), context)

            verify(repository).unvote("Main Base", player)
            verify(player).sendRichMessage("<gray>You removed your vote for Main Base</gray>")
        }

    @Test
    fun `BuildVoteCommand votes and announces the build`() =
        runTest {
            val server = mock(Server::class.java)
            whenever(plugin.server).thenReturn(server)
            setContext("Main Base")
            whenever(player.name).thenReturn("builder")
            whenever(repository.vote("Main Base", player)).thenReturn(build(name = "Main Base"))

            invokeHandler(BuildVoteCommand(plugin, mock(), repository), context)

            verify(repository).vote("Main Base", player)
            verify(player).sendRichMessage("<green>You voted for Main Base</green>")
            verify(server).broadcast(any<Component>())
        }

    @Test
    fun `BuildCommand teleports the player to the stored build location`() =
        runTest {
            val world = mock(World::class.java)
            val server = mock(Server::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            whenever(plugin.server).thenReturn(server)
            setContext("Main Base")
            whenever(repository.get("Main Base")).thenReturn(build(name = "Main Base"))
            whenever(server.getWorld("world")).thenReturn(world)

            invokeHandler(BuildCommand(plugin, mock(), repository, server, teleporter), context)

            verify(teleporter).move(
                eq(player),
                eq(Location(world, 1.5, 64.0, -2.0, 30f, 20f)),
                eq(PlayerTeleportEvent.TeleportCause.COMMAND),
                eq(PlayerTeleporter.TeleportOptions(preloadDestinationChunk = true)),
            )
            verify(player).showTitle(any<Title>())
            verify(player).sendRichMessage("<gray>Teleported to <aqua>Main Base</aqua></gray>")
        }

    @Test
    fun `BuildsCommand installs all build subcommands`() {
        val command =
            BuildsCommand(
                BuildCreateCommand(plugin, repository),
                BuildDeleteCommand(plugin, mock(), repository),
                BuildEditCommand(plugin, mock(), repository),
                BuildListCommand(plugin, repository),
                BuildMoveCommand(plugin, mock(), repository),
                BuildSetCommand(plugin, repository),
                BuildUnvoteCommand(plugin, mock(), repository),
                BuildVoteCommand(plugin, mock(), repository),
            ).literal()

        assertEquals(
            setOf("create", "delete", "edit", "list", "move", "set", "unvote", "vote"),
            command.children.map { it.name }.toSet(),
        )
    }

    private fun setContext(name: String) {
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/builds $name")
        whenever(source.sender).thenReturn(player)
        whenever(context.getArgument("name", String::class.java)).thenReturn(name)
    }

    private fun setupPlayerLocation(): Location {
        val world = mock(World::class.java)
        val location = Location(world, 1.5, 64.0, -2.0, 20f, 30f)
        whenever(player.location).thenReturn(location)
        whenever(world.name).thenReturn("world")
        setContext("unused")
        return location
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

private suspend fun invokeHandler(
    command: Any,
    context: PaperCommandContext,
) {
    val handler =
        command.javaClass.getDeclaredMethod(
            "execute",
            CommandContext::class.java,
            kotlin.coroutines.Continuation::class.java,
        )
    handler.isAccessible = true
    kotlin.coroutines.suspendCoroutine<Unit> { continuation ->
        try {
            val result = handler.invoke(command, context, continuation)
            if (result !== kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED) {
                continuation.resumeWith(Result.success(Unit))
            }
        } catch (exception: java.lang.reflect.InvocationTargetException) {
            continuation.resumeWith(Result.failure(exception.targetException))
        }
    }
}
