package com.projectcitybuild.modules.mutes.actions

import com.projectcitybuild.repositories.PlayerConfigRepository
import java.util.UUID

class MutePlayer(
    private val playerConfigRepository: PlayerConfigRepository,
) {
    fun execute(
        targetPlayerUUID: UUID,
        shouldMute: Boolean,
    ) {
        val targetPlayerConfig = playerConfigRepository
            .get(targetPlayerUUID)!!
            .also { it.isMuted = shouldMute }

        playerConfigRepository.save(targetPlayerConfig)
    }
}
