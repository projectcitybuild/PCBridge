package com.projectcitybuild.modules.moderation.mutes.actions

import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.entity.Player

class MutePlayer(
    private val playerConfigRepository: PlayerConfigRepository,
) {
    fun execute(
        targetPlayer: Player,
        shouldMute: Boolean,
    ) {
        val targetPlayerConfig = playerConfigRepository
            .get(targetPlayer.uniqueId)!!
            .also { it.isMuted = shouldMute }

        playerConfigRepository.save(targetPlayerConfig)
    }
}
