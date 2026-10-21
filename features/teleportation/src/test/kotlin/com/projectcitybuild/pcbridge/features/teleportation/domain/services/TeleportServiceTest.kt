package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.TeleportationPermissions
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportRequest
import com.projectcitybuild.pcbridge.features.teleportation.domain.validation.TeleportValidator
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.runtime.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class TeleportServiceTest {
    private val server = mock<Server>()
    private val plugin = mock<JavaPlugin>()
    private val timer = mock<SpigotTimer>()
    private val playerTeleporter = mock<PlayerTeleporter>()
    private val historyStorage = mock<TeleportHistoryStorage>()
    private val cooldown = mock<Cooldown>()
    private val findRandomLocation = mock<FindRandomLocation>()
    private val validator = TeleportValidator()
    private val requests = TeleportRequestService()
    private val application =
        TeleportService(
            plugin = plugin,
            server = server,
            playerTeleporter = playerTeleporter,
            historyStorage = historyStorage,
            validator = validator,
            requestService = requests,
            timer = timer,
            cooldown = cooldown,
            findRandomLocation = findRandomLocation,
        )

    @Test
    fun `random teleport requires its permission before searching`() = runBlocking {
        val player = player("Steve")

        application.random(player)

        verify(player).sendMessage(l10n.teleportation.errorTeleportPermission)
        verifyNoInteractions(cooldown, findRandomLocation, playerTeleporter)
    }

    @Test
    fun `random teleport uses the shared movement path and reports the destination`() = runBlocking {
        val world = mock<World>()
        val player = player("Steve", TeleportationPermissions.RANDOM)
        val candidate = Location(world, 12.0, 0.0, -8.0)
        val destination = Location(world, 12.5, 64.0, -8.5)
        whenever(player.location).thenReturn(destination)
        whenever(findRandomLocation.candidates(player, attempts = 5)).thenReturn(listOf(candidate))

        application.random(player)

        verify(cooldown).throttle(5.seconds, player, "rtp")
        verify(player).sendMessage(l10n.teleportation.searchingForSafeLocation)
        verify(playerTeleporter).move(
            eq(player),
            eq(candidate),
            eq(TeleportCause.COMMAND),
            eq(
                PlayerTeleporter.TeleportOptions(
                    adjustYForSafety = true,
                    snapToBlockCenter = true,
                    playSound = false,
                ),
            ),
        )
        verify(player).sendMessage(l10n.teleportation.teleportedToCoordinate(12, 64, -8))
    }

    @Test
    fun `random teleport reports failure when no candidates are available`() = runBlocking {
        val player = player("Steve", TeleportationPermissions.RANDOM)
        whenever(findRandomLocation.candidates(player, attempts = 5)).thenReturn(emptyList())

        application.random(player)

        verify(player).sendMessage(l10n.teleportation.errorCouldNotFindSafeLocation)
        verifyNoInteractions(playerTeleporter)
    }

    @Test
    fun `force and silent flags require their dedicated permissions`() = runBlocking {
        val requester = player("Steve", TeleportationPermissions.REQUEST)
        val target = player("Alex")

        application.to(requester, target, TeleportFlags(force = true, silent = true))

        verify(requester).sendMessage(l10n.teleportation.errorTeleportPermission)
        verify(target, never()).sendMessage(any<Component>())
        verifyNoInteractions(playerTeleporter)
    }

    @Test
    fun `silent force teleport keeps sender result and suppresses destination notice`() = runBlocking {
        val requester =
            player(
                "Steve",
                TeleportationPermissions.REQUEST,
                TeleportationPermissions.FORCE,
                TeleportationPermissions.SILENT,
            )
        val target = player("Alex")
        whenever(target.location).thenReturn(Location(mock<World>(), 10.0, 70.0, 20.0))

        application.to(requester, target, TeleportFlags(force = true, silent = true))

        verify(playerTeleporter).move(
            eq(requester),
            any<Location>(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        verify(requester).sendMessage(any<Component>())
        verify(target, never()).sendMessage(any<Component>())
    }

    @Test
    fun `accepting one incoming request triggers only one teleport`() = runBlocking {
        val target = player("Alex")
        val requester = player("Steve")
        val requesterId = requester.uniqueId
        val targetId = target.uniqueId
        requests.create(requesterId, requester.name, targetId, target.name, silent = false)
        whenever(server.getPlayer(requesterId)).thenReturn(requester)
        whenever(target.location).thenReturn(Location(mock<World>(), 10.0, 70.0, 20.0))

        application.accept(target, requesterId = null)
        application.accept(target, requesterId = null)

        verify(playerTeleporter).move(
            eq(requester),
            any<Location>(),
            eq(TeleportCause.COMMAND),
            any(),
        )
    }

    @Test
    fun `to and here commands create consent requests in the correct directions`() = runBlocking {
        val requester = player("Steve", TeleportationPermissions.REQUEST)
        val target = player("Alex")
        val hereRequester = player("Casey", TeleportationPermissions.OTHERS)
        val hereTarget = player("Devon")
        val requesterLocation = Location(mock<World>(), 1.0, 64.0, 1.0)
        whenever(server.getPlayer(requester.uniqueId)).thenReturn(requester)
        whenever(server.getPlayer(hereRequester.uniqueId)).thenReturn(hereRequester)
        whenever(requester.location).thenReturn(requesterLocation)
        whenever(hereRequester.location).thenReturn(requesterLocation)

        application.to(requester, target)
        application.here(hereRequester, hereTarget)

        assertEquals(
            2,
            mockingDetails(timer).invocations.count { it.method.name.startsWith("scheduleOnce") },
        )

        val toRequest = requests.takeIncoming(target.uniqueId, requester.uniqueId)
            as TeleportRequestService.TakeResult.Taken
        val hereRequest = requests.takeIncoming(hereTarget.uniqueId, hereRequester.uniqueId)
            as TeleportRequestService.TakeResult.Taken
        assertEquals(TeleportRequest.Direction.TO, toRequest.request.direction)
        assertEquals(TeleportRequest.Direction.HERE, hereRequest.request.direction)

        requests.createHere(
            requesterId = hereRequester.uniqueId,
            requesterName = hereRequester.name,
            targetId = hereTarget.uniqueId,
            targetName = hereTarget.name,
            silent = false,
        )
        application.accept(hereTarget, hereRequester.uniqueId)

        val destination = argumentCaptor<Location>()
        verify(playerTeleporter).move(
            eq(hereTarget),
            destination.capture(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        assertEquals(requesterLocation.world, destination.firstValue.world)
        assertEquals(requesterLocation.x, destination.firstValue.x)
        verify(hereRequester).sendMessage(l10n.teleportation.teleportReceived(hereTarget.name))
    }

    @Test
    fun `send requests the selected player to teleport to the destination`() = runBlocking {
        val world = mock<World>()
        val operator = player("Staff", TeleportationPermissions.OTHERS, TeleportationPermissions.SILENT)
        val selected = player("Alex")
        val target = player("Casey")
        val targetLocation = Location(world, 5.0, 70.0, 5.0)
        whenever(server.getPlayer(target.uniqueId)).thenReturn(target)
        whenever(target.location).thenReturn(targetLocation)

        application.send(operator, selected, target, TeleportFlags(silent = true))

        verifyNoInteractions(playerTeleporter)
        verify(selected).sendMessage(l10n.teleportation.teleportHereRequestReceived(target.name))

        application.accept(selected, target.uniqueId)

        val destination = argumentCaptor<Location>()
        verify(playerTeleporter).move(
            eq(selected),
            destination.capture(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        assertSame(world, destination.firstValue.world)
        assertEquals(targetLocation.x, destination.firstValue.x)
        verify(target, never()).sendMessage(l10n.teleportation.teleportReceived(selected.name))
    }

    @Test
    fun `send force requires force permission and moves only when authorized`() = runBlocking {
        val target = player("Casey")
        val targetLocation = Location(mock<World>(), 5.0, 70.0, 5.0)
        whenever(target.location).thenReturn(targetLocation)
        val selected = player("Alex")
        val unauthorized = player("Staff", TeleportationPermissions.OTHERS)

        application.send(unauthorized, selected, target, TeleportFlags(force = true))

        verify(unauthorized).sendMessage(l10n.teleportation.errorTeleportPermission)
        verifyNoInteractions(playerTeleporter)

        val authorized = player("Admin", TeleportationPermissions.OTHERS, TeleportationPermissions.FORCE)
        application.send(authorized, selected, target, TeleportFlags(force = true))

        verify(playerTeleporter).move(
            eq(selected),
            any<Location>(),
            eq(TeleportCause.COMMAND),
            any(),
        )
    }

    @Test
    fun `all requests every other online player to teleport to the destination`() = runBlocking {
        val operator = player("Staff", TeleportationPermissions.ALL)
        val target = player("Target")
        val first = player("Alex")
        val second = player("Casey")
        val targetLocation = Location(mock<World>(), 5.0, 70.0, 5.0)
        whenever(target.location).thenReturn(targetLocation)
        whenever(server.onlinePlayers).thenReturn(listOf(target, first, second))
        whenever(server.getPlayer(target.uniqueId)).thenReturn(target)

        application.all(operator, target)

        verifyNoInteractions(playerTeleporter)
        application.accept(first, target.uniqueId)
        application.accept(second, target.uniqueId)

        val destinations = argumentCaptor<Location>()
        verify(playerTeleporter, times(2)).move(
            any(),
            destinations.capture(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        assertEquals(2, destinations.allValues.size)
        destinations.allValues.forEach { destination ->
            assertEquals(targetLocation.world, destination.world)
            assertEquals(targetLocation.x, destination.x)
        }
    }

    @Test
    fun `all force requires force permission and moves every other online player`() = runBlocking {
        val target = player("Target")
        val first = player("Alex")
        val second = player("Casey")
        whenever(target.location).thenReturn(Location(mock<World>(), 5.0, 70.0, 5.0))
        whenever(server.onlinePlayers).thenReturn(listOf(target, first, second))
        val unauthorized = player("Staff", TeleportationPermissions.ALL)

        application.all(unauthorized, target, TeleportFlags(force = true))

        verify(unauthorized).sendMessage(l10n.teleportation.errorTeleportPermission)
        verifyNoInteractions(playerTeleporter)

        val authorized = player("Admin", TeleportationPermissions.ALL, TeleportationPermissions.FORCE)
        application.all(authorized, target, TeleportFlags(force = true))

        verify(playerTeleporter, times(2)).move(
            any(),
            any<Location>(),
            eq(TeleportCause.COMMAND),
            any(),
        )
    }

    @Test
    fun `force here requires force permission in addition to staff permission`() = runBlocking {
        val operator = player("Steve", TeleportationPermissions.OTHERS)
        val target = player("Alex")

        application.here(operator, target, TeleportFlags(force = true))

        verify(operator).sendMessage(l10n.teleportation.errorTeleportPermission)
        verifyNoInteractions(playerTeleporter)
    }

    @Test
    fun `force here keeps the canonical staff operation and shared movement path`() = runBlocking {
        val operator = player("Steve", TeleportationPermissions.OTHERS, TeleportationPermissions.FORCE)
        val target = player("Alex")
        val operatorLocation = Location(mock<World>(), 5.0, 70.0, 5.0)
        whenever(operator.location).thenReturn(operatorLocation)

        application.here(operator, target, TeleportFlags(force = true))

        val destination = argumentCaptor<Location>()
        verify(playerTeleporter).move(
            eq(target),
            destination.capture(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        assertEquals(operatorLocation.world, destination.firstValue.world)
        assertEquals(operatorLocation.x, destination.firstValue.x)
    }

    @Test
    fun `deny and cancel consume the canonical request state`() {
        val requester = player("Steve")
        val target = player("Alex")
        whenever(server.getPlayer(requester.uniqueId)).thenReturn(requester)
        whenever(server.getPlayer(target.uniqueId)).thenReturn(target)
        requests.create(requester.uniqueId, requester.name, target.uniqueId, target.name, silent = false)

        application.deny(target, requester.uniqueId)

        verify(target).sendMessage(l10n.teleportation.teleportRequestDenied(requester.name))
        verify(requester).sendMessage(l10n.teleportation.teleportRequestWasDenied(target.name))

        requests.create(requester.uniqueId, requester.name, target.uniqueId, target.name, silent = false)
        application.cancel(requester)

        verify(requester).sendMessage(l10n.teleportation.teleportRequestCancelled(target.name))
        verify(target).sendMessage(l10n.teleportation.teleportRequestWasCancelled(requester.name))
    }

    @Test
    fun `back reports missing history`() = runBlocking {
        val player = player("Steve", TeleportationPermissions.BACK)
        whenever(historyStorage.retrieve(player.uniqueId)).thenReturn(TeleportHistoryStorage.Lookup.Missing)

        application.back(player, player)

        verify(player).sendMessage(l10n.teleportation.errorTeleportHistoryMissing)
        verifyNoInteractions(playerTeleporter)
    }

    @Test
    fun `position defaults to the current world and preserves coordinate precision`() = runBlocking {
        val world = mock<World>()
        val player = player("Steve", TeleportationPermissions.POSITION)
        whenever(player.world).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 1.0, 65.0, 1.0, 30.0f, 10.0f))

        application.position(
            player = player,
            x = 12.125,
            y = 64.75,
            z = -8.625,
        )

        val destination = argumentCaptor<Location>()
        verify(playerTeleporter).move(
            eq(player),
            destination.capture(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        assertEquals(world, destination.firstValue.world)
        Assertions.assertEquals(12.125, destination.firstValue.x)
        Assertions.assertEquals(64.75, destination.firstValue.y)
        Assertions.assertEquals(-8.625, destination.firstValue.z)
        Assertions.assertEquals(30.0f, destination.firstValue.yaw)
        Assertions.assertEquals(10.0f, destination.firstValue.pitch)
    }

    @Test
    fun `position uses supplied yaw and pitch`() = runBlocking {
        val world = mock<World>()
        val player = player("Steve", TeleportationPermissions.POSITION)
        whenever(player.world).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 1.0, 65.0, 1.0, 30.0f, 10.0f))

        application.position(
            player = player,
            x = 12.0,
            y = 64.0,
            z = -8.0,
            yaw = 180.0f,
            pitch = -45.0f,
        )

        val destination = argumentCaptor<Location>()
        verify(playerTeleporter).move(
            eq(player),
            destination.capture(),
            eq(TeleportCause.COMMAND),
            any(),
        )
        Assertions.assertEquals(180.0f, destination.firstValue.yaw)
        Assertions.assertEquals(-45.0f, destination.firstValue.pitch)
    }

    private fun player(
        name: String,
        vararg permissions: PermissionNode,
    ): Player {
        val player = mock<Player>()
        val id = UUID.randomUUID()
        whenever(player.name).thenReturn(name)
        whenever(player.uniqueId).thenReturn(id)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(any<String>())).thenReturn(false)
        doNothing().whenever(player).sendMessage(any<Component>())
        permissions.forEach { whenever(player.hasPermission(it.node)).thenReturn(true) }
        return player
    }
}
