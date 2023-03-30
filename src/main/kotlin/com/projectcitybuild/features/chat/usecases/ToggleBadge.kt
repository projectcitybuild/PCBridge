package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.repositories.PlayerConfigRepository
import java.util.UUID

class ToggleBadge(
    private val playerConfigRepository: PlayerConfigRepository,
) {
    fun execute(willBeDisabled: Boolean, playerUUID: UUID) {
        val playerConfig = playerConfigRepository
            .get(playerUUID)!!
            .also { it.isChatBadgeDisabled = willBeDisabled }

        playerConfigRepository.save(playerConfig)
    }
}
