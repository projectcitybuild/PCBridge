package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.entity.Player

class PlayerTimeService {
    fun current(player: Player): PlayerTimeState? = if (player.isPlayerTimeRelative && player.playerTimeOffset == 0L) {
        null
    } else {
        PlayerTimeState(
            ticks = player.playerTime,
            relative = player.isPlayerTimeRelative,
        )
    }

    fun set(
        player: Player,
        ticks: Long,
        relative: Boolean,
    ) {
        var playerTime = player.playerTime
        playerTime -= playerTime % TICKS_PER_DAY
        playerTime += TICKS_PER_DAY + ticks
        if (relative) {
            playerTime -= player.world.time
        }
        player.setPlayerTime(playerTime, relative)
    }

    fun reset(player: Player) {
        player.resetPlayerTime()
    }

    private companion object {
        const val TICKS_PER_DAY = 24_000L
    }
}

data class PlayerTimeState(
    val ticks: Long,
    val relative: Boolean,
)
