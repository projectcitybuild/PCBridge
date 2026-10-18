package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportRequest
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.runtime.cooldowns.Cooldown
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.SafeDestinationNotFoundException
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportFailedException
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

class TeleportApplication(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val playerTeleporter: PlayerTeleporter,
    private val historyStorage: TeleportHistoryStorage,
    private val validationService: TeleportValidationService,
    private val requestService: TeleportRequestService,
    private val timer: SpigotTimer,
    private val cooldown: Cooldown,
    private val findRandomLocation: FindRandomLocation,
) {
    suspend fun to(
        requester: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ) {
        if (!validationService.to(requester, target, flags)) return

        if (!flags.force) {
            createRequest(
                requester = requester,
                target = target,
                silent = flags.silent,
            )
            return
        }
        val moved = move(
            player = requester,
            destination = target.location.clone(),
            recipient = target,
            silent = flags.silent,
        )
        if (moved) {
            requester.sendMessage(l10n.teleportCompleted(target.name))
        }
    }

    suspend fun accept(
        target: Player,
        requesterId: UUID?,
    ) {
        when (val result = requestService.takeIncoming(targetId = target.uniqueId, requesterId)) {
            TeleportRequestService.TakeResult.Ambiguous -> {
                target.sendMessage(l10n.errorTeleportRequestAmbiguous)
                return
            }
            TeleportRequestService.TakeResult.Missing -> {
                target.sendMessage(l10n.errorTeleportRequestMissing)
                return
            }
            TeleportRequestService.TakeResult.Expired -> {
                target.sendMessage(l10n.errorTeleportRequestExpired)
                return
            }
            is TeleportRequestService.TakeResult.Taken -> {
                val request = result.request
                val requester = server.getPlayer(request.requesterId)
                if (requester == null || !requester.isOnline || !target.isOnline) {
                    target.sendMessage(l10n.errorTeleportTargetOffline)
                    requester?.sendMessage(l10n.errorTeleportTargetOffline)
                    return
                }
                val playerToMove = if (request.direction == TeleportRequest.Direction.TO) requester else target
                val destinationPlayer = if (request.direction == TeleportRequest.Direction.TO) target else requester
                val moved = move(
                    player = playerToMove,
                    destination = destinationPlayer.location.clone(),
                    recipient = destinationPlayer,
                    silent = request.silent,
                )
                if (moved) {
                    target.sendMessage(l10n.teleportRequestAccepted(requester.name))
                    requester.sendMessage(
                        if (request.direction == TeleportRequest.Direction.TO) {
                            l10n.teleportCompleted(target.name)
                        } else {
                            l10n.teleportOperationSucceeded(target.name, requester.name)
                        },
                    )
                }
            }
        }
    }

    fun deny(
        target: Player,
        requesterId: UUID?,
    ) {
        when (val result = requestService.takeIncoming(targetId = target.uniqueId, requesterId)) {
            TeleportRequestService.TakeResult.Ambiguous -> {
                target.sendMessage(l10n.errorTeleportRequestAmbiguous)
            }
            TeleportRequestService.TakeResult.Missing -> {
                target.sendMessage(l10n.errorTeleportRequestMissing)
            }
            TeleportRequestService.TakeResult.Expired -> {
                target.sendMessage(l10n.errorTeleportRequestExpired)
            }
            is TeleportRequestService.TakeResult.Taken -> {
                val request = result.request
                target.sendMessage(l10n.teleportRequestDenied(request.requesterName))
                server.getPlayer(request.requesterId)?.sendMessage(
                    l10n.teleportRequestWasDenied(target.name),
                )
            }
        }
    }

    fun cancel(requester: Player) {
        cancel(requester, targetId = null)
    }

    fun cancel(
        requester: Player,
        targetId: UUID?,
    ) {
        val requests = requestService.cancel(requester.uniqueId, targetId)
        if (requests.isEmpty()) {
            requester.sendMessage(l10n.errorTeleportRequestMissing)
            return
        }
        requests.forEach { request ->
            requester.sendMessage(l10n.teleportRequestCancelled(request.targetName))
            server.getPlayer(request.targetId)?.sendMessage(
                l10n.teleportRequestWasCancelled(requester.name),
            )
        }
    }

    suspend fun here(
        operator: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ) {
        if (!validationService.here(operator, target, flags)) return

        if (!flags.force) {
            createRequest(
                requester = operator,
                target = target,
                silent = flags.silent,
                direction = TeleportRequest.Direction.HERE,
            )
            return
        }
        val moved = move(
            player = target,
            destination = operator.location.clone(),
            recipient = operator,
            silent = flags.silent,
        )
        if (moved) {
            operator.sendMessage(l10n.teleportOperationSucceeded(target.name, operator.name))
        }
    }

    fun requestHere(
        requester: Player,
        target: Player,
    ) {
        if (!validationService.requestHere(requester, target)) return

        createRequest(
            requester = requester,
            target = target,
            silent = false,
            direction = TeleportRequest.Direction.HERE,
        )
    }

    suspend fun send(
        operator: CommandSender,
        player: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ) {
        if (!validationService.send(operator, player, target, flags)) return

        if (!flags.force) {
            val result = createRequest(
                requester = target,
                target = player,
                silent = flags.silent,
                direction = TeleportRequest.Direction.HERE,
            )
            val operatorId = (operator as? Player)?.uniqueId
            if (result !is TeleportRequestService.CreateResult.Created ||
                (operatorId != target.uniqueId && operatorId != player.uniqueId)
            ) {
                operator.sendMessage(
                    when (result) {
                        is TeleportRequestService.CreateResult.Created -> l10n.teleportRequestCreated(
                            player.name,
                            ttl = result.request.ttl(),
                        )
                        TeleportRequestService.CreateResult.AlreadyPending -> l10n.errorTeleportRequestPending
                        TeleportRequestService.CreateResult.SelfTarget -> l10n.errorTeleportSelf
                    },
                )
            }
            return
        }
        val moved = move(
            player = player,
            destination = target.location.clone(),
            recipient = target,
            silent = flags.silent,
        )
        if (moved) {
            operator.sendMessage(l10n.teleportOperationSucceeded(player.name, target.name))
        }
    }

    suspend fun all(
        operator: CommandSender,
        requestedTarget: Player?,
        flags: TeleportFlags = TeleportFlags(),
    ) {
        val target = validationService.all(operator, requestedTarget, flags)
            ?: return

        if (!flags.force) {
            val players = server.onlinePlayers.filter {
                it.uniqueId != target.uniqueId
            }
            val playersById = players.associateBy { it.uniqueId }
            val recipients = players.map {
                TeleportRequestService.Recipient(it.uniqueId, it.name)
            }
            when (
                val result = requestService.createHereForMany(
                    requesterId = target.uniqueId,
                    requesterName = target.name,
                    recipients = recipients,
                    silent = flags.silent,
                )
            ) {
                is TeleportRequestService.CreateManyResult.Created -> {
                    result.requests.forEach { request ->
                        notifyRequestTarget(
                            target = playersById[request.targetId],
                            request = request,
                            direction = TeleportRequest.Direction.HERE,
                        )
                    }
                    notifyOperatorAndRequester(
                        operator,
                        target,
                        l10n.teleportRequestsCreated(result.requests.size),
                    )
                }
                TeleportRequestService.CreateManyResult.NoRecipients -> {
                    notifyOperatorAndRequester(operator, target, l10n.teleportRequestsCreated(0))
                }
                TeleportRequestService.CreateManyResult.SelfTarget -> {
                    notifyOperatorAndRequester(operator, target, l10n.errorTeleportSelf)
                }
                TeleportRequestService.CreateManyResult.AlreadyPending -> {
                    notifyOperatorAndRequester(operator, target, l10n.errorTeleportRequestPending)
                }
            }
            return
        }

        val destination = target.location.clone()
        val players = server.onlinePlayers.filter {
            it.uniqueId != target.uniqueId
        }
        var succeeded = 0
        var failed = 0
        players.forEach { player ->
            try {
                val options = PlayerTeleporter.TeleportOptions(
                    playSound = players.size < 5,
                )
                val moved = move(
                    player = player,
                    destination = destination.clone(),
                    recipient = target,
                    silent = flags.silent,
                    options = options,
                )
                if (moved) {
                    succeeded++
                } else {
                    failed++
                }
            } catch (exception: Exception) {
                failed++
                logSync.error(exception) {
                    "Bulk teleport failed for player ${player.uniqueId} to ${target.uniqueId}"
                }
            }
        }
        operator.sendMessage(l10n.teleportBulkSummary(succeeded, failed))
    }

    suspend fun position(
        player: Player,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float? = null,
        pitch: Float? = null,
    ) {
        if (!validationService.position(player, x, y, z, yaw, pitch)) return

        val currentLocation = player.location
        val destination = Location(
            player.world,
            x,
            y,
            z,
            yaw ?: currentLocation.yaw,
            pitch ?: currentLocation.pitch,
        )
        if (move(player, destination, recipient = null, silent = true)) {
            player.sendMessage(l10n.teleportedToCoordinate(x, y, z))
        }
    }

    suspend fun random(player: Player) {
        if (!validationService.random(player)) return

        cooldown.throttle(5.seconds, player, "rtp")
        player.sendMessage(l10n.searchingForSafeLocation)

        val options = PlayerTeleporter.TeleportOptions(
            adjustYForSafety = true,
            snapToBlockCenter = true,
        )
        for (destination in findRandomLocation.candidates(player, attempts = 5)) {
            val moved = move(
                player = player,
                destination = destination,
                recipient = null,
                silent = true,
                options = options,
                reportFailure = false,
            )
            if (moved) {
                val location = player.location
                player.sendMessage(
                    l10n.teleportedToCoordinate(location.x.toInt(), location.y.toInt(), location.z.toInt()),
                )
                return
            }
        }
        player.sendMessage(l10n.errorCouldNotFindSafeLocation)
    }

    suspend fun back(
        operator: CommandSender,
        target: Player,
    ) {
        if (!validationService.back(operator, target)) return

        when (val history = historyStorage.retrieve(target.uniqueId)) {
            TeleportHistoryStorage.Lookup.Missing -> {
                operator.sendMessage(l10n.errorTeleportHistoryMissing)
            }

            TeleportHistoryStorage.Lookup.Unavailable -> {
                operator.sendMessage(l10n.errorTeleportHistoryWorldUnavailable)
            }

            is TeleportHistoryStorage.Lookup.Found -> {
                if (move(target, history.location, recipient = null, silent = true)) {
                    operator.sendMessage(l10n.teleportBackCompleted(target.name))
                }
            }
        }
    }

    private suspend fun move(
        player: Player,
        destination: Location,
        recipient: Player?,
        silent: Boolean,
        options: PlayerTeleporter.TeleportOptions = PlayerTeleporter.TeleportOptions(),
        reportFailure: Boolean = true,
    ): Boolean {
        try {
            playerTeleporter.move(
                player = player,
                destination = destination,
                cause = TeleportCause.COMMAND,
                options = options.copy(playSound = !silent),
            )
        } catch (_: SafeDestinationNotFoundException) {
            if (reportFailure) {
                player.sendMessage(l10n.errorCouldNotFindSafeLocation)
            }
            return false
        } catch (_: TeleportFailedException) {
            if (reportFailure) {
                player.sendMessage(l10n.errorTeleportFailed)
            }
            return false
        }

        if (!silent && recipient != null && recipient.uniqueId != player.uniqueId && recipient.isOnline) {
            recipient.sendMessage(l10n.teleportReceived(player.name))
        }
        return true
    }

    private fun createRequest(
        requester: Player,
        target: Player,
        silent: Boolean,
        direction: TeleportRequest.Direction = TeleportRequest.Direction.TO,
    ): TeleportRequestService.CreateResult {
        val result = when (direction) {
            TeleportRequest.Direction.TO -> {
                requestService.create(
                    requesterId = requester.uniqueId,
                    requesterName = requester.name,
                    targetId = target.uniqueId,
                    targetName = target.name,
                    silent = silent,
                )
            }
            TeleportRequest.Direction.HERE -> {
                requestService.createHere(
                    requesterId = requester.uniqueId,
                    requesterName = requester.name,
                    targetId = target.uniqueId,
                    targetName = target.name,
                    silent = silent,
                )
            }
        }

        when (result) {
            is TeleportRequestService.CreateResult.Created -> {
                requester.sendMessage(l10n.teleportRequestCreated(target.name, ttl = result.request.ttl()))
                notifyRequestTarget(target, result.request, direction)
            }
            TeleportRequestService.CreateResult.AlreadyPending -> {
                requester.sendMessage(l10n.errorTeleportRequestPending)
            }
            TeleportRequestService.CreateResult.SelfTarget -> {
                requester.sendMessage(l10n.errorTeleportSelf)
            }
        }
        return result
    }

    private fun notifyRequestTarget(
        target: Player?,
        request: TeleportRequest,
        direction: TeleportRequest.Direction,
    ) {
        target?.sendMessage(
            if (direction == TeleportRequest.Direction.TO) {
                l10n.teleportRequestReceived(request.requesterName)
            } else {
                l10n.teleportHereRequestReceived(request.requesterName)
            },
        )
        scheduleExpiration(request)
    }

    private fun scheduleExpiration(request: TeleportRequest) {
        timer.scheduleOnce(
            identifier = "teleport-request-${request.requesterId}-${request.targetId}",
            delay = request.timeUntilExpiry(),
            work = {
                val expired = requestService.expire(request.requesterId, request.targetId, request.createdAt)
                if (expired != null) {
                    withContext(plugin.minecraftDispatcher) {
                        server.getPlayer(expired.requesterId)?.sendMessage(
                            l10n.teleportRequestSentExpired(expired.targetName),
                        )
                        server.getPlayer(expired.targetId)?.sendMessage(
                            l10n.teleportRequestExpired(expired.requesterName),
                        )
                    }
                }
            },
        )
    }

    private fun notifyOperatorAndRequester(
        operator: CommandSender,
        requester: Player,
        message: Component,
    ) {
        operator.sendMessage(message)
        if ((operator as? Player)?.uniqueId != requester.uniqueId) {
            requester.sendMessage(message)
        }
    }
}

private fun TeleportRequest.ttl() = Duration
    .between(Instant.now(), expiresAt)
    .let { remaining ->
        Duration.ofSeconds(remaining.seconds + if (remaining.nano == 0) 0 else 1)
    }.toKotlinDuration()

private fun TeleportRequest.timeUntilExpiry(): kotlin.time.Duration {
    val remaining = Duration.between(Instant.now(), expiresAt).toKotlinDuration()
    if (remaining.isNegative()) return kotlin.time.Duration.ZERO

    val milliseconds = remaining.inWholeMilliseconds
    val hasSubMillisecondRemainder = remaining.inWholeNanoseconds % 1_000_000L != 0L
    return (milliseconds + if (hasSubMillisecondRemainder) 1 else 0).milliseconds
}
