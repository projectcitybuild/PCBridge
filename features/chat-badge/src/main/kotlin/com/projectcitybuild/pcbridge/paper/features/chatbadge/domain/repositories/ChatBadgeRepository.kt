package com.projectcitybuild.pcbridge.paper.features.chatbadge.domain.repositories

import com.projectcitybuild.pcbridge.paper.features.chatbadge.domain.ChatBadgeFormatter
import com.projectcitybuild.pcbridge.paper.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.runtime.state.store.SessionStore
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatBadgeRepository(
    private val remoteConfig: RemoteConfig,
    private val session: SessionStore,
    private val badgeCache: Cache<UUID, CachedComponent>,
    private val badgeFormatter: ChatBadgeFormatter,
) {
    data class CachedComponent(val value: Component?)

    suspend fun getComponent(playerUUID: UUID): CachedComponent {
        return badgeCache.get(playerUUID) {
            val playerSession = session.state.players[playerUUID]
            val badges =
                playerSession?.syncedValue?.badges
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
