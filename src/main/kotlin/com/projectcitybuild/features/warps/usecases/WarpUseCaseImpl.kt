package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.features.warps.repositories.QueuedWarpRepository
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import javax.inject.Inject

class WarpUseCaseImpl @Inject constructor(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val queuedWarpRepository: QueuedWarpRepository,
    private val nameGuesser: NameGuesser,
    private val logger: PlatformLogger,
): WarpUseCase {

    override fun warp(
        targetWarpName: String,
        playerServerName: String,
        player: Player,
    ): Result<WarpUseCase.WarpEvent, WarpUseCase.FailureReason> {
        val availableWarps = warpRepository.all()
        val availableWarpNames = availableWarps.map { it.name }

        val warpName = nameGuesser.guessClosest(targetWarpName, availableWarpNames)
            ?: return Failure(WarpUseCase.FailureReason.WARP_DOES_NOT_EXIST)

        val warp = availableWarps.first { it.name == warpName }

        Bukkit.getPluginManager().callEvent(
            PlayerPreWarpEvent(player, player.location)
        )

        val isWarpOnSameServer = playerServerName == warp.location.serverName
        if (isWarpOnSameServer) {
            val worldName = warp.location.worldName
            val world = plugin.server.getWorld(worldName)
            if (world == null) {
                logger.warning("Could not find world matching name [$worldName] for warp")
                return Failure(WarpUseCase.FailureReason.WORLD_NOT_FOUND)
            }
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
        } else {
            queuedWarpRepository.queue(player.uniqueId, warp)

            MessageToBungeecord(
                plugin,
                player,
                SubChannel.SWITCH_PLAYER_SERVER,
                arrayOf(
                    player.uniqueId.toString(),
                    warp.location.serverName,
                )
            ).send()
        }

        return Success(
            WarpUseCase.WarpEvent(
                warpName = warp.name,
                isSameServer = isWarpOnSameServer,
            )
        )
    }
}