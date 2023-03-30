package com.projectcitybuild.features.chat

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.permissions.Permissions
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.util.UUID

class ChatGroupFormatter(
    private val permissions: Permissions,
    private val config: Config,
) {
    data class Aggregate(
        val prefix: List<BaseComponent>,
        val suffix: List<BaseComponent>,
        val groups: TextComponent,
    )

    enum class GroupType {
        TRUST,
        BUILD,
        DONOR,
    }

    private val groupPriorities: MutableMap<String, Pair<GroupType, Int>> = mutableMapOf()
    private val cache: MutableMap<UUID, Aggregate> = mutableMapOf()

    fun get(playerUUID: UUID): Aggregate {
        val cached = cache[playerUUID]
        if (cached != null) {
            return cached
        }
        return buildAggregate(playerUUID = playerUUID)
            .also { cache[playerUUID] = it }
    }

    fun flushAllCaches() {
        cache.clear()
        groupPriorities.clear()
    }

    fun flush(playerUUID: UUID) {
        cache.remove(playerUUID)
    }

    private fun buildAggregate(playerUUID: UUID): Aggregate {
        if (groupPriorities.isEmpty()) {
            buildGroupList()
        }

        val groupNames = permissions.getUserGroups(playerUUID)

        var highestTrust: Pair<Int, String>? = null
        var highestBuild: Pair<Int, String>? = null
        var highestDonor: Pair<Int, String>? = null

        for (groupName in groupNames) {
            val lowercaseGroupName = groupName.lowercase()
            val (groupType, priorityIndex) = groupPriorities[lowercaseGroupName]
                ?: continue

            when (groupType) {
                GroupType.TRUST -> if (highestTrust == null || priorityIndex < highestTrust.first) {
                    highestTrust = Pair(priorityIndex, groupName)
                }
                GroupType.BUILD -> if (highestBuild == null || priorityIndex < highestBuild.first) {
                    highestBuild = Pair(priorityIndex, groupName)
                }
                GroupType.DONOR -> if (highestDonor == null || priorityIndex < highestDonor.first) {
                    highestDonor = Pair(priorityIndex, groupName)
                }
            }
        }

        val groupTC = TextComponent()

        if (highestDonor != null) {
            groupTextComponent(highestDonor).forEach { groupTC.addExtra(it) }
        }
        if (highestTrust != null) {
            groupTextComponent(highestTrust).forEach { groupTC.addExtra(it) }
        }
        if (highestBuild != null) {
            groupTextComponent(highestBuild).forEach { groupTC.addExtra(it) }
        }

        val prefix = TextComponent.fromLegacyText(permissions.getUserPrefix(playerUUID)).toList()
        val suffix = TextComponent.fromLegacyText(permissions.getUserSuffix(playerUUID)).toList()

        return Aggregate(
            prefix = prefix,
            suffix = suffix,
            groups = groupTC,
        )
    }

    private fun groupTextComponent(group: Pair<Int, String>): Array<out BaseComponent> {
        val groupName = group.second
        val hoverName = permissions.getGroupMetaData(groupName = groupName, key = "chat_hover_name")
        val displayName = permissions.getGroupMetaData(groupName = groupName, key = "chat_display_name")
            ?: permissions.getGroupDisplayName(groupName)
            ?: group.second

        return TextComponent
            .fromLegacyText(displayName)
            .also { tc ->
                if (!hoverName.isNullOrEmpty()) {
                    tc.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverName)) }
                }
            }
    }

    private fun buildGroupList() {
        val trustedGroupPriority = config.get(ConfigKeys.groupsTrustPriority)
        val builderGroupPriority = config.get(ConfigKeys.groupsBuildPriority)
        val donorGroupPriority = config.get(ConfigKeys.groupsDonorPriority)

        trustedGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(GroupType.TRUST, it.index)
        }
        builderGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(GroupType.BUILD, it.index)
        }
        donorGroupPriority.withIndex().forEach {
            groupPriorities[it.value.lowercase()] = Pair(GroupType.DONOR, it.index)
        }
    }
}
