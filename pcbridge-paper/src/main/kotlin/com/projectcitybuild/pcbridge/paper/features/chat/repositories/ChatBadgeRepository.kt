package com.projectcitybuild.pcbridge.paper.features.chat.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.features.chat.ChatBadgeFormatter
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatBadgeRepository(
    private val remoteConfig: RemoteConfig,
    private val store: Store,
    private val badgeCache: Cache<UUID, CachedComponent>,
    private val badgeFormatter: ChatBadgeFormatter,
) {
    data class CachedComponent(val value: Component?)

    suspend fun getComponent(playerUUID: UUID): CachedComponent {
        return badgeCache.get(playerUUID) {
            val badges = store.state.players[playerUUID]?.badges
                ?: emptyList()

            val config = remoteConfig.latest.config
            val icon = config.chat.badgeIcon

            CachedComponent(badgeFormatter.format(badges, icon))
        }
    }

    fun invalidate(playerUUID: UUID) {
        badgeCache.invalidate(playerUUID)
    }

    fun invalidateAll() = badgeCache.invalidateAll()
}
