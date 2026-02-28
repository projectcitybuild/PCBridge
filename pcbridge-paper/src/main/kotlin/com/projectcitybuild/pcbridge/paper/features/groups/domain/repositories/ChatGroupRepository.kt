package com.projectcitybuild.pcbridge.paper.features.groups.domain.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.features.groups.domain.ChatGroupFormatter
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatGroupRepository(
    private val session: SessionStore,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val groupCache: Cache<UUID, CachedComponent>,
) {
    data class CachedComponent(val value: Component?)

    suspend fun getGroupsComponent(playerUUID: UUID): CachedComponent {
        return groupCache.get(playerUUID) {
            val playerSession = session.state.players[playerUUID]
            val groups = playerSession?.syncedValue?.roles
                ?: emptyList()

            withContext(Dispatchers.IO) {
                CachedComponent(chatGroupFormatter.format(groups.toSet()))
            }
        }
    }

    fun invalidate(playerUUID: UUID) {
        groupCache.invalidate(playerUUID)
    }
}
