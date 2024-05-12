package com.projectcitybuild.pcbridge.features.chat.repositories

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.state.Store
import com.projectcitybuild.pcbridge.http.responses.Badge
import java.util.UUID

class ChatBadgeRepository(
    private val config: Config,
    private val store: Store,
) {
    fun getIcon(): String {
        return config.get().chatBadge.icon
    }

    fun getBadgesForPlayer(playerUuid: UUID): List<Badge> {
        return store.state.players[playerUuid]?.badges
            ?: emptyList()
    }
}
