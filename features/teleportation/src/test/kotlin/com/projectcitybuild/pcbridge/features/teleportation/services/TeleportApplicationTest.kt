package com.projectcitybuild.pcbridge.features.teleportation.services

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.runtime.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class TeleportApplicationTest {
    private val server = mock<Server>()
    private val scheduler = mock<BukkitScheduler>()
    private val plugin = mock<Plugin>()
    private val playerTeleporter = mock<PlayerTeleporter>()
    private val historyStorage = mock<TeleportHistoryStorage>()
    private val cooldown = mock<Cooldown>()
    private val findRandomLocation = mock<FindRandomLocation>()
    private val requests = TeleportRequestService()
    private val application =
        TeleportApplication(
            plugin = plugin,
            server = server,
            playerTeleporter = playerTeleporter,
            historyStorage = historyStorage,
            requestService = requests,
            cooldown = cooldown,
            findRandomLocation = findRandomLocation,
        )

    init {
        doReturn(scheduler).whenever(server).scheduler
        doReturn(mock<BukkitTask>()).whenever(scheduler).runTaskLater(
            any(),
            any<Runnable>(),
            any(),
        )
    }

    @Test
    fun `random teleport requires its permission before searching`() =
        kotlinx.coroutines.runBlocking {
            val player = player("Alex")

            application.random(player)

            verify(player).sendMessage(l10n.errorTeleportPermission)
            verifyNoInteractions(cooldown, findRandomLocation, playerTeleporter)
        }

    @Test
    fun `random teleport uses the shared movement path and reports the destination`() =
        kotlinx.coroutines.runBlocking {
            val world = mock<World>()
            val player = player("Alex", PermissionNode.TELEPORT_RANDOM)
            val candidate = Location(world, 12.0, 0.0, -8.0)
            val destination = Location(world, 12.5, 64.0, -8.5)
            whenever(player.location).thenReturn(destination)
            whenever(findRandomLocation.candidates(player, attempts = 5)).thenReturn(listOf(candidate))

            application.random(player)

            verify(cooldown).throttle(5.seconds, player, "rtp")
            verify(player).sendMessage(l10n.searchingForSafeLocation)
            verify(playerTeleporter).move(
                eq(player),
                eq(candidate),
                eq(TeleportCause.COMMAND),
                eq(
                    PlayerTeleporter.TeleportOptions(
                        adjustYForSafety = true,
                        snapToBlockCenter = true,
                    ),
                ),
            )
            verify(player).sendMessage(l10n.teleportedToCoordinate(12, 64, -8))
        }

    @Test
    fun `random teleport reports failure when no candidates are available`() =
        kotlinx.coroutines.runBlocking {
            val player = player("Alex", PermissionNode.TELEPORT_RANDOM)
            whenever(findRandomLocation.candidates(player, attempts = 5)).thenReturn(emptyList())

            application.random(player)

            verify(player).sendMessage(l10n.errorCouldNotFindSafeLocation)
            verifyNoInteractions(playerTeleporter)
        }

    @Test
    fun `force and silent flags require their dedicated permissions`() =
        kotlinx.coroutines.runBlocking {
            val requester = player("Alex", PermissionNode.TELEPORT_REQUEST)
            val target = player("Blair")

            application.to(requester, target, TeleportFlags(force = true, silent = true))

            verify(requester).sendMessage(l10n.errorTeleportPermission)
            verify(target, never()).sendMessage(any<Component>())
            verifyNoInteractions(playerTeleporter)
        }

    @Test
    fun `silent force teleport keeps sender result and suppresses destination notice`() =
        kotlinx.coroutines.runBlocking {
            val requester =
                player(
                    "Alex",
                    PermissionNode.TELEPORT_REQUEST,
                    PermissionNode.TELEPORT_FORCE,
                    PermissionNode.TELEPORT_SILENT,
                )
            val target = player("Blair")
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
    fun `accepting one incoming request triggers only one teleport`() =
        kotlinx.coroutines.runBlocking {
            val target = player("Blair")
            val requester = player("Alex")
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
    fun `to and here commands create consent requests in the correct directions`() =
        kotlinx.coroutines.runBlocking {
            val requester = player("Alex", PermissionNode.TELEPORT_REQUEST)
            val target = player("Blair")
            val hereRequester = player("Casey", PermissionNode.TELEPORT_OTHERS)
            val hereTarget = player("Devon")
            val requesterLocation = Location(mock<World>(), 1.0, 64.0, 1.0)
            whenever(server.getPlayer(requester.uniqueId)).thenReturn(requester)
            whenever(server.getPlayer(hereRequester.uniqueId)).thenReturn(hereRequester)
            whenever(requester.location).thenReturn(requesterLocation)
            whenever(hereRequester.location).thenReturn(requesterLocation)

            application.to(requester, target)
            application.here(hereRequester, hereTarget)

            val toRequest =
                requests.takeIncoming(target.uniqueId, requester.uniqueId)
                    as TeleportRequestService.TakeResult.Taken
            val hereRequest =
                requests.takeIncoming(hereTarget.uniqueId, hereRequester.uniqueId)
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
            verify(hereRequester).sendMessage(l10n.teleportReceived(hereTarget.name))
        }

    @Test
    fun `send requests the selected player to teleport to the destination`() =
        kotlinx.coroutines.runBlocking {
            val operator = player("Staff", PermissionNode.TELEPORT_OTHERS, PermissionNode.TELEPORT_SILENT)
            val selected = player("Blair")
            val target = player("Casey")
            val targetLocation = Location(mock<World>(), 5.0, 70.0, 5.0)
            whenever(server.getPlayer(target.uniqueId)).thenReturn(target)
            whenever(target.location).thenReturn(targetLocation)

            application.send(operator, selected, target, TeleportFlags(silent = true))

            verifyNoInteractions(playerTeleporter)
            verify(selected).sendMessage(l10n.teleportHereRequestReceived(target.name))

            application.accept(selected, target.uniqueId)

            val destination = argumentCaptor<Location>()
            verify(playerTeleporter).move(
                eq(selected),
                destination.capture(),
                eq(TeleportCause.COMMAND),
                any(),
            )
            assertEquals(targetLocation.world, destination.firstValue.world)
            assertEquals(targetLocation.x, destination.firstValue.x)
            verify(target, never()).sendMessage(l10n.teleportReceived(selected.name))
        }

    @Test
    fun `send force requires force permission and moves only when authorized`() =
        kotlinx.coroutines.runBlocking {
            val target = player("Casey")
            val targetLocation = Location(mock<World>(), 5.0, 70.0, 5.0)
            whenever(target.location).thenReturn(targetLocation)
            val selected = player("Blair")
            val unauthorized = player("Staff", PermissionNode.TELEPORT_OTHERS)

            application.send(unauthorized, selected, target, TeleportFlags(force = true))

            verify(unauthorized).sendMessage(l10n.errorTeleportPermission)
            verifyNoInteractions(playerTeleporter)

            val authorized = player("Admin", PermissionNode.TELEPORT_OTHERS, PermissionNode.TELEPORT_FORCE)
            application.send(authorized, selected, target, TeleportFlags(force = true))

            verify(playerTeleporter).move(
                eq(selected),
                any<Location>(),
                eq(TeleportCause.COMMAND),
                any(),
            )
        }

    @Test
    fun `all requests every other online player to teleport to the destination`() =
        kotlinx.coroutines.runBlocking {
            val operator = player("Staff", PermissionNode.TELEPORT_ALL)
            val target = player("Target")
            val first = player("Blair")
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
    fun `all force requires force permission and moves every other online player`() =
        kotlinx.coroutines.runBlocking {
            val target = player("Target")
            val first = player("Blair")
            val second = player("Casey")
            whenever(target.location).thenReturn(Location(mock<World>(), 5.0, 70.0, 5.0))
            whenever(server.onlinePlayers).thenReturn(listOf(target, first, second))
            val unauthorized = player("Staff", PermissionNode.TELEPORT_ALL)

            application.all(unauthorized, target, TeleportFlags(force = true))

            verify(unauthorized).sendMessage(l10n.errorTeleportPermission)
            verifyNoInteractions(playerTeleporter)

            val authorized = player("Admin", PermissionNode.TELEPORT_ALL, PermissionNode.TELEPORT_FORCE)
            application.all(authorized, target, TeleportFlags(force = true))

            verify(playerTeleporter, times(2)).move(
                any(),
                any<Location>(),
                eq(TeleportCause.COMMAND),
                any(),
            )
        }

    @Test
    fun `force here requires force permission in addition to staff permission`() =
        kotlinx.coroutines.runBlocking {
            val operator = player("Alex", PermissionNode.TELEPORT_OTHERS)
            val target = player("Blair")

            application.here(operator, target, TeleportFlags(force = true))

            verify(operator).sendMessage(l10n.errorTeleportPermission)
            verifyNoInteractions(playerTeleporter)
        }

    @Test
    fun `force here keeps the canonical staff operation and shared movement path`() =
        kotlinx.coroutines.runBlocking {
            val operator = player("Alex", PermissionNode.TELEPORT_OTHERS, PermissionNode.TELEPORT_FORCE)
            val target = player("Blair")
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
        val requester = player("Alex")
        val target = player("Blair")
        whenever(server.getPlayer(requester.uniqueId)).thenReturn(requester)
        whenever(server.getPlayer(target.uniqueId)).thenReturn(target)
        requests.create(requester.uniqueId, requester.name, target.uniqueId, target.name, silent = false)

        application.deny(target, requester.uniqueId)

        verify(target).sendMessage(l10n.teleportRequestDenied(requester.name))
        verify(requester).sendMessage(l10n.teleportRequestWasDenied(target.name))

        requests.create(requester.uniqueId, requester.name, target.uniqueId, target.name, silent = false)
        application.cancel(requester)

        verify(requester).sendMessage(l10n.teleportRequestCancelled(target.name))
        verify(target).sendMessage(l10n.teleportRequestWasCancelled(requester.name))
    }

    @Test
    fun `EssentialsX permissions do not grant teleport operations`() =
        kotlinx.coroutines.runBlocking {
            val requester = player("Alex")
            val target = player("Blair")
            whenever(requester.hasPermission("essentials.tp")).thenReturn(true)
            whenever(requester.hasPermission("essentials.tpo")).thenReturn(true)

            application.to(requester, target)
            application.to(requester, target, TeleportFlags(force = true))

            verify(requester, org.mockito.kotlin.times(2)).sendMessage(l10n.errorTeleportPermission)
            verifyNoInteractions(playerTeleporter)
        }

    @Test
    fun `back reports missing history`() =
        kotlinx.coroutines.runBlocking {
            val player = player("Alex", PermissionNode.TELEPORT_BACK)
            whenever(historyStorage.retrieve(player.uniqueId)).thenReturn(TeleportHistoryStorage.Lookup.Missing)

            application.back(player, player)

            verify(player).sendMessage(l10n.errorTeleportHistoryMissing)
            verifyNoInteractions(playerTeleporter)
        }

    @Test
    fun `position defaults to the current world and preserves coordinate precision`() =
        kotlinx.coroutines.runBlocking {
            val world = mock<World>()
            val player = player("Alex", PermissionNode.TELEPORT_POSITION)
            whenever(player.world).thenReturn(world)
            whenever(player.location).thenReturn(Location(world, 1.0, 65.0, 1.0, 30.0f, 10.0f))

            application.position(
                player = player,
                x = 12.125,
                y = 64.75,
                z = -8.625,
                worldName = null,
            )

            val destination = argumentCaptor<Location>()
            verify(playerTeleporter).move(
                eq(player),
                destination.capture(),
                eq(TeleportCause.COMMAND),
                any(),
            )
            org.junit.jupiter.api.Assertions.assertEquals(world, destination.firstValue.world)
            org.junit.jupiter.api.Assertions.assertEquals(12.125, destination.firstValue.x)
            org.junit.jupiter.api.Assertions.assertEquals(64.75, destination.firstValue.y)
            org.junit.jupiter.api.Assertions.assertEquals(-8.625, destination.firstValue.z)
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
