package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.events.PlayerPreWarpEvent
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player

class TeleportToWarp(
    private val warpRepository: WarpRepository,
    private val nameGuesser: NameGuesser,
    private val logger: PlatformLogger,
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val server: Server,
) {
    data class Warp(
        val warpName: String,
    )
    enum class FailureReason {
        WARP_NOT_FOUND,
        WORLD_NOT_FOUND,
    }

    fun warp(
        player: Player,
        targetWarpName: String,
    ): Result<Warp, FailureReason> {
        val availableWarpNames = warpRepository.names()

        val matchingWarpName = nameGuesser.guessClosest(targetWarpName, availableWarpNames)
            ?: return Failure(FailureReason.WARP_NOT_FOUND)

        val warp = warpRepository.first(matchingWarpName)
            ?: return Failure(FailureReason.WARP_NOT_FOUND)

        val world = server.getWorld(warp.location.worldName)
        if (world == null) {
            logger.warning("Could not find world matching name [${warp.location.worldName}] for warp")
            return Failure(FailureReason.WORLD_NOT_FOUND)
        }

        localEventBroadcaster.emit(
            PlayerPreWarpEvent(player = player)
        )

        player.teleport(
            Location(
                world,
                warp.location.x,
                warp.location.y,
                warp.location.z,
                warp.location.yaw,
                warp.location.pitch,
            )
        )
        return Success(Warp(warpName = warp.name))
    }
}
