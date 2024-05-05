package com.projectcitybuild.features.chat.repositories

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.features.chat.ChatGroupType
import com.projectcitybuild.libs.permissions.Permissions
import com.projectcitybuild.pcbridge.core.modules.config.Config
import java.util.UUID

class ChatGroupRepository(
    private val permissions: Permissions,
    private val config: Config<PluginConfig>,
) {
    fun getGroupNamesForPlayer(playerUUID: UUID): Set<String> {
        return permissions.getUserGroups(playerUUID)
    }

    fun getPrefixForPlayer(playerUUID: UUID): String {
        return permissions.getUserPrefix(playerUUID)
    }

    fun getSuffixForPlayer(playerUUID: UUID): String {
        return permissions.getUserSuffix(playerUUID)
    }

    fun getGroupDisplayName(groupName: String): String? {
        return permissions.getGroupMetaData(
            groupName = groupName,
            key = "chat_display_name",
        ) ?: permissions.getGroupDisplayName(groupName)
    }

    fun getGroupHoverName(groupName: String): String? {
        return permissions.getGroupMetaData(
            groupName = groupName,
            key = "chat_hover_name",
        )
    }

    fun getDisplayPriority(groupType: ChatGroupType): List<String> {
        return when (groupType) {
            ChatGroupType.TRUST -> config.get().groups.displayPriority.trust
            ChatGroupType.BUILD -> config.get().groups.displayPriority.builder
            ChatGroupType.DONOR -> config.get().groups.displayPriority.donor
        }
    }
}