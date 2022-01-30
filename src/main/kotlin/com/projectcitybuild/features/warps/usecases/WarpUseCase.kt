package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Result
import org.bukkit.entity.Player

interface WarpUseCase {
    data class WarpEvent(
        val warpName: String,
        val isSameServer: Boolean
    )
    enum class FailureReason {
        WARP_DOES_NOT_EXIST,
        WORLD_NOT_FOUND,
    }
    fun warp(
        targetWarpName: String,
        playerServerName: String,
        player: Player,
    ): Result<WarpEvent, FailureReason>
}
