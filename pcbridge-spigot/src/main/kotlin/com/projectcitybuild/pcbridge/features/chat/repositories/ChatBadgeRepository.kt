package com.projectcitybuild.pcbridge.features.chat.repositories

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.state.Store
import com.projectcitybuild.pcbridge.features.chat.ChatBadgeFormatter
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatBadgeRepository(
    private val config: Config,
    private val store: Store,
    private val badgeCache: Cache<UUID, Component>,
    private val badgeFormatter: ChatBadgeFormatter,
) {
    suspend fun getComponent(playerUUID: UUID): Component {
        return badgeCache.get(playerUUID) {
            val badges = store.state.players[playerUUID]?.badges
                ?: emptyList()

            val icon = config.get().chatBadge.icon

            badgeFormatter.format(badges, icon)
        }
    }

    fun invalidate(playerUUID: UUID) {
        badgeCache.invalidate(playerUUID)
    }
}
