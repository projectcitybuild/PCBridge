package com.projectcitybuild.pcbridge.features.chat.repositories

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.permissions.Permissions
import com.projectcitybuild.pcbridge.features.chat.ChatGroupFormatter
import com.projectcitybuild.pcbridge.features.chat.ChatGroupType
import io.github.reactivecircus.cache4k.Cache
import java.util.UUID

class ChatGroupRepository(
    private val permissions: Permissions,
    private val config: Config,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val groupCache: Cache<UUID, ChatGroupFormatter.Aggregate>,
) {
    suspend fun getAggregate(playerUUID: UUID): ChatGroupFormatter.Aggregate {
        return groupCache.get(playerUUID) {
            val groupNames = permissions.getUserGroups(playerUUID)

            chatGroupFormatter.format(
                groupNames = groupNames,
                groupDisplayNames = groupNames.fold(mutableMapOf()) { map, groupName ->
                    map[groupName] = permissions.getGroupMetaData(
                        groupName = groupName,
                        key = "chat_display_name",
                    ) ?: permissions.getGroupDisplayName(groupName)
                    map
                },
                groupHoverNames = groupNames.fold(mutableMapOf()) { map, groupName ->
                    map[groupName] = permissions.getGroupMetaData(
                        groupName = groupName,
                        key = "chat_display_name",
                    ) ?: permissions.getGroupDisplayName(groupName)
                    map
                },
                groupDisplayPriorities = mapOf(
                    Pair(ChatGroupType.TRUST, config.get().groups.displayPriority.trust),
                    Pair(ChatGroupType.BUILD, config.get().groups.displayPriority.builder),
                    Pair(ChatGroupType.DONOR, config.get().groups.displayPriority.donor),
                ),
                suffix = permissions.getUserSuffix(playerUUID),
                prefix = permissions.getUserPrefix(playerUUID),
            )
        }
    }

    fun invalidate(playerUUID: UUID) {
        groupCache.invalidate(playerUUID)
    }
}
