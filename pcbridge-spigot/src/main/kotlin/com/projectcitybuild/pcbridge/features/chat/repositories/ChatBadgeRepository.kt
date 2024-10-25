package com.projectcitybuild.pcbridge.features.chat.repositories

import com.projectcitybuild.pcbridge.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.core.store.Store
import com.projectcitybuild.pcbridge.features.chat.ChatBadgeFormatter
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatBadgeRepository(
    private val remoteConfig: RemoteConfig,
    private val store: Store,
    private val badgeCache: Cache<UUID, Component>,
    private val badgeFormatter: ChatBadgeFormatter,
) {
    suspend fun getComponent(playerUUID: UUID): Component {
        return badgeCache.get(playerUUID) {
            val badges = store.state.players[playerUUID]?.badges
                ?: emptyList()

            val config = remoteConfig.latest.config
            val icon = config.chat.badgeIcon

            badgeFormatter.format(badges, icon)
        }
    }

    fun invalidate(playerUUID: UUID) {
        badgeCache.invalidate(playerUUID)
    }
}
