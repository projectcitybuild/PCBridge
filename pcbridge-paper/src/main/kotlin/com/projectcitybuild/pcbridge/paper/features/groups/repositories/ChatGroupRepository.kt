package com.projectcitybuild.pcbridge.paper.features.groups.repositories

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.features.groups.ChatGroupFormatter
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatGroupRepository(
    private val store: Store,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val groupCache: Cache<UUID, CachedComponent>,
) {
    data class CachedComponent(val value: Component?)

    suspend fun getGroupsComponent(playerUUID: UUID): CachedComponent {
        return groupCache.get(playerUUID) {
            val groups = store.state.players[playerUUID]?.groups
                ?: emptyList()

            CachedComponent(chatGroupFormatter.format(groups.toSet()))
        }
    }

    fun invalidate(playerUUID: UUID) {
        groupCache.invalidate(playerUUID)
    }

    fun invalidateAll() = groupCache.invalidateAll()
}
