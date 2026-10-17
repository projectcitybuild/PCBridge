package com.projectcitybuild.pcbridge.features.homes.hooks.commands

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Home
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.HomeLimit
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PaginatedList
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes.HomeRenameCommand
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
import java.time.LocalDateTime
import java.util.UUID

class HomeCommandHandlersTest {
    private val plugin = mock(Plugin::class.java)
    private val repository = mock(HomeRepository::class.java)
    private val player = mock(Player::class.java)
    private val source = mock(CommandSourceStack::class.java)
    private val context = mock(PaperCommandContext::class.java)
    private val playerId = UUID.randomUUID()

    init {
        whenever(context.input).thenReturn("/homes")
    }

    @Test
    fun `HomeCreateCommand creates a home using the player's location`() =
        runTest {
            val location = playerLocation()
            whenever(context.getArgument("name", String::class.java)).thenReturn("Mine")
            whenever(repository.create("Mine", location, player)).thenReturn(home(name = "Mine"))

            invokeHandler(HomeCreateCommand(plugin, repository), context)

            verify(repository).create("Mine", location, player)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.homeCreated("Mine"))
        }

    @Test
    fun `HomeDeleteCommand deletes the requested home for its owner`() =
        runTest {
            setContext("Cabin")

            invokeHandler(HomeDeleteCommand(plugin, mock(), repository), context)

            verify(repository).delete(player, "Cabin")
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.homeDeleted("Cabin"))
        }

    @Test
    fun `HomeLimitCommand reports a player with no home allowance`() =
        runTest {
            setContext()
            whenever(repository.limit(player)).thenReturn(HomeLimit(max = 0, current = 0, sources = emptyMap()))

            invokeHandler(HomeLimitCommand(plugin, repository), context)

            verify(player).sendRichMessage("<gray>You cannot create a home</gray>")
        }

    @Test
    fun `HomeLimitCommand displays allowance counts and role sources`() =
        runTest {
            setContext()
            whenever(repository.limit(player)).thenReturn(HomeLimit(max = 5, current = 2, sources = mapOf("VIP" to 2)))

            invokeHandler(HomeLimitCommand(plugin, repository), context)

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
            val remoteConfig = mock(RemoteConfig::class.java)
            whenever(remoteConfig.latest).thenReturn(RemoteConfigVersion(1, RemoteConfigKeyValues()))
            whenever(context.getArgument("page", Int::class.java)).thenReturn(2)
            whenever(repository.all(playerId, 2, 15)).thenReturn(PaginatedList(emptyList(), 2, 15, 0, 0))
            setContext()

            invokeHandler(HomeListCommand(plugin, repository, remoteConfig), context)

            verify(repository).all(playerId, 2, 15)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.errorPageNotFound)
        }

    @Test
    fun `HomeMoveCommand moves the named home to the player's current location`() =
        runTest {
            val location = playerLocation()
            whenever(context.getArgument("name", String::class.java)).thenReturn("Cabin")
            whenever(repository.move("Cabin", location, player)).thenReturn(home(name = "Cabin"))

            invokeHandler(HomeMoveCommand(plugin, mock(), repository), context)

            verify(repository).move("Cabin", location, player)
            verify(player).sendMessage(com.projectcitybuild.pcbridge.core.l10n.l10n.homeMoved("Cabin"))
        }

    @Test
    fun `HomeRenameCommand looks up the home before building its rename dialog`() =
        runTest {
            setContext("Cabin")
            whenever(repository.get(playerId, "Cabin")).thenReturn(home(name = "Cabin"))

            invokeHandler(HomeRenameCommand(plugin, mock(), repository), context)

            verify(repository).get(playerId, "Cabin")
        }

    @Test
    fun `HomeCommand teleports to the stored home coordinates`() =
        runTest {
            val server = mock(Server::class.java)
            val world = mock(World::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            setContext("Cabin")
            whenever(repository.get(playerId, "Cabin")).thenReturn(home(name = "Cabin"))
            whenever(server.getWorld("world")).thenReturn(world)

            invokeHandler(HomeCommand(plugin, server, mock(), repository, teleporter), context)

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
            val server = mock(Server::class.java)
            val teleporter = mock(PlayerTeleporter::class.java)
            setContext("Cabin")
            whenever(repository.get(playerId, "Cabin")).thenReturn(home(name = "Cabin"))

            invokeHandler(HomeCommand(plugin, server, mock(), repository, teleporter), context)

            verify(teleporter, never()).move(any(), any(), any(), any())
        }

    @Test
    fun `HomesCommand registers its subcommands and default list handler`() {
        val command =
            HomesCommand(
                HomeCreateCommand(plugin, repository),
                HomeDeleteCommand(plugin, HomeNameSuggester(repository), repository),
                HomeListCommand(plugin, repository, mock()),
                HomeMoveCommand(plugin, HomeNameSuggester(repository), repository),
                HomeLimitCommand(plugin, repository),
                HomeRenameCommand(plugin, HomeNameSuggester(repository), repository),
            ).literal()

        assertEquals(
            setOf("create", "delete", "list", "move", "limit", "rename"),
            command.children.map { it.name }.toSet(),
        )
        assertEquals(true, command.command != null)
    }

    private fun setContext(name: String? = null) {
        whenever(context.source).thenReturn(source)
        whenever(context.input).thenReturn("/homes ${name.orEmpty()}")
        whenever(source.sender).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)
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

    private fun home(name: String) = Home(1, name, "world", 10.0, 70.0, -5.0, 15f, 45f, LocalDateTime.MIN, LocalDateTime.MIN)
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
