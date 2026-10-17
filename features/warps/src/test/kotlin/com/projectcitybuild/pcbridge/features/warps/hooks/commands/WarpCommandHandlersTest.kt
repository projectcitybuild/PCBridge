package com.projectcitybuild.pcbridge.features.warps.hooks.commands

import com.mojang.brigadier.context.CommandContext
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
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
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
    private val context = mock(PaperCommandContext::class.java)

    init {
        whenever(context.input).thenReturn("/warps")
    }

    @Test
    fun `WarpCreateCommand creates a warp and broadcasts the create event`() =
        runTest {
            val location = playerLocation()
            whenever(context.getArgument("name", String::class.java)).thenReturn("Market")
            whenever(repository.create("Market", location)).thenReturn(warp(name = "Market"))
            val broadcaster = mock(SpigotEventBroadcaster::class.java)

            invokeHandler(WarpCreateCommand(plugin, repository, broadcaster), context)

            verify(repository).create("Market", location)
            verify(broadcaster).broadcast(any<WarpCreateEvent>())
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.warpCreated("Market"))
        }

    @Test
    fun `WarpDeleteCommand deletes the warp and broadcasts the delete event`() =
        runTest {
            setContext("Market")
            val broadcaster = mock(SpigotEventBroadcaster::class.java)

            invokeHandler(WarpDeleteCommand(plugin, mock(), repository, broadcaster), context)

            verify(repository).delete("Market")
            verify(broadcaster).broadcast(any<WarpDeleteEvent>())
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.warpDeleted("Market"))
        }

    @Test
    fun `WarpListCommand fetches the configured page and reports a missing page`() =
        runTest {
            setContext()
            whenever(context.getArgument("page", Int::class.java)).thenReturn(4)
            val remoteConfig = mock(RemoteConfig::class.java)
            whenever(remoteConfig.latest).thenReturn(RemoteConfigVersion(1, RemoteConfigKeyValues()))
            whenever(repository.all(4, 15)).thenReturn(PaginatedList(emptyList(), 4, 15, 0, 0))

            invokeHandler(WarpListCommand(plugin, repository, remoteConfig), context)

            verify(repository).all(4, 15)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.errorPageNotFound)
        }

    @Test
    fun `WarpMoveCommand updates the named warp to the player's current location`() =
        runTest {
            val location = playerLocation()
            whenever(context.getArgument("name", String::class.java)).thenReturn("Market")

            invokeHandler(WarpMoveCommand(plugin, mock(), repository), context)

            verify(repository).move("Market", location)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.warpMoved("Market"))
        }

    @Test
    fun `WarpRenameCommand looks up the warp before building its rename dialog`() =
        runTest {
            setContext("Market")
            whenever(repository.get("Market")).thenReturn(warp(name = "Market"))

            invokeHandler(WarpRenameCommand(plugin, mock(), repository), context)

            verify(repository).get("Market")
        }

    @Test
    fun `WarpCommand teleports the player to the warp coordinates`() =
        runTest {
            val server = mock(Server::class.java)
            val world = mock(World::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            setContext("Market")
            whenever(repository.get("Market")).thenReturn(warp(name = "Market"))
            whenever(server.getWorld("world")).thenReturn(world)

            invokeHandler(WarpCommand(plugin, mock(), repository, server, teleporter), context)

            verify(teleporter).move(
                eq(player),
                eq(Location(world, 10.0, 70.0, -5.0, 45f, 15f)),
                eq(PlayerTeleportEvent.TeleportCause.COMMAND),
                eq(PlayerTeleporter.TeleportOptions()),
            )
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.teleportedToName("Market"))
        }

    @Test
    fun `WarpCommand does not teleport when the destination world is missing`() =
        runTest {
            val server = mock(Server::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            setContext("Market")
            whenever(repository.get("Market")).thenReturn(warp(name = "Market"))

            invokeHandler(WarpCommand(plugin, mock(), repository, server, teleporter), context)

            verify(teleporter, never()).move(any(), any(), any(), any())
        }

    @Test
    fun `WarpsCommand registers its subcommands and default list handler`() {
        val command =
            WarpsCommand(
                WarpCreateCommand(plugin, repository, mock()),
                WarpDeleteCommand(plugin, WarpNameSuggester(repository), repository, mock()),
                WarpListCommand(plugin, repository, mock()),
                WarpMoveCommand(plugin, WarpNameSuggester(repository), repository),
                WarpRenameCommand(plugin, WarpNameSuggester(repository), repository),
            ).literal()

        assertEquals(
            setOf("create", "delete", "list", "move", "rename"),
            command.children.map { it.name }.toSet(),
        )
        assertEquals(true, command.command != null)
    }

    private fun setContext(name: String? = null) {
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/warps ${name.orEmpty()}")
        whenever(source.sender).thenReturn(player)
        if (name != null) {
            whenever(context.getArgument("name", String::class.java)).thenReturn(name)
        }
    }

    private fun playerLocation(): Location {
        val world = mock(World::class.java)
        val location = Location(world, 10.0, 70.0, -5.0, 15f, 45f)
        whenever(world.name).thenReturn("world")
        whenever(player.location).thenReturn(location)
        setContext()
        return location
    }

    private fun warp(name: String) = Warp(17, name, "world", 10.0, 70.0, -5.0, 15f, 45f)
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
