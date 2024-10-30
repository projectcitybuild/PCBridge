package com.projectcitybuild.pcbridge.paper.features.chat.repositories

import com.projectcitybuild.pcbridge.paper.core.store.Store
import com.projectcitybuild.pcbridge.paper.features.chat.ChatGroupFormatter
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import java.util.UUID

class ChatGroupRepository(
    private val store: Store,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val groupCache: Cache<UUID, Component>,
) {
    suspend fun getGroupsComponent(playerUUID: UUID): Component = groupCache.get(playerUUID) {
        val groups = store.state.players[playerUUID]?.groups
            ?: emptyList()

        chatGroupFormatter.format(groups.toSet())
    }

    fun invalidate(playerUUID: UUID) {
        groupCache.invalidate(playerUUID)
    }

    fun invalidateAll() = groupCache.invalidateAll()
}
