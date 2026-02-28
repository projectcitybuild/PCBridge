package com.projectcitybuild.pcbridge.paper.features.roles.domain.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.features.roles.domain.ChatRoleFormatter
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatRoleRepository(
    private val session: SessionStore,
    private val chatRoleFormatter: ChatRoleFormatter,
    private val roleCache: Cache<UUID, CachedComponent>,
) {
    data class CachedComponent(val value: Component?)

    suspend fun getRolesComponent(playerUUID: UUID): CachedComponent {
        return roleCache.get(playerUUID) {
            val playerSession = session.state.players[playerUUID]
            val roles = playerSession?.syncedValue?.roles
                ?: emptyList()

            withContext(Dispatchers.IO) {
                CachedComponent(chatRoleFormatter.format(roles.toSet()))
            }
        }
    }

    fun invalidate(playerUUID: UUID) {
        roleCache.invalidate(playerUUID)
    }
}
