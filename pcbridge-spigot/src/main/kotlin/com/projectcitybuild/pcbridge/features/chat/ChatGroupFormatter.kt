package com.projectcitybuild.pcbridge.features.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatGroupFormatter {
    data class Aggregate(
        val prefix: Component,
        val suffix: Component,
        val groups: Component,
    )

    private val groupPriorities: MutableMap<String, Pair<ChatGroupType, Int>> = mutableMapOf()

    fun format(
        groupNames: Set<String>,
        groupDisplayNames: Map<String, String?>,
        groupHoverNames: Map<String, String?>,
        groupDisplayPriorities: Map<ChatGroupType, List<String>>,
        prefix: String,
        suffix: String,
    ): Aggregate {
        if (groupPriorities.isEmpty()) {
            buildGroupList(groupDisplayPriorities)
        }

        var highestTrust: Pair<Int, String>? = null
        var highestBuild: Pair<Int, String>? = null
        var highestDonor: Pair<Int, String>? = null

        for (groupName in groupNames) {
            val lowercaseGroupName = groupName.lowercase()
            val (groupType, priorityIndex) =
                groupPriorities[lowercaseGroupName]
                    ?: continue

            when (groupType) {
                ChatGroupType.TRUST ->
                    if (highestTrust == null || priorityIndex < highestTrust.first) {
                        highestTrust = Pair(priorityIndex, groupName)
                    }
                ChatGroupType.BUILD ->
                    if (highestBuild == null || priorityIndex < highestBuild.first) {
                        highestBuild = Pair(priorityIndex, groupName)
                    }
                ChatGroupType.DONOR ->
                    if (highestDonor == null || priorityIndex < highestDonor.first) {
                        highestDonor = Pair(priorityIndex, groupName)
                    }
            }
        }

        val groupComponent = Component.text()

        if (highestDonor != null) {
            groupComponent.append(
                groupTextComponent(
                    highestDonor,
                    hoverName = groupHoverNames[highestDonor.second],
                    displayName = groupDisplayNames[highestDonor.second],
                )
            )
        }
        if (highestTrust != null) {
            groupComponent.append(
                groupTextComponent(
                    highestTrust,
                    hoverName = groupHoverNames[highestTrust.second],
                    displayName = groupDisplayNames[highestTrust.second],
                )
            )
        }
        if (highestBuild != null) {
            groupComponent.append(
                groupTextComponent(
                    highestBuild,
                    hoverName = groupHoverNames[highestBuild.second],
                    displayName = groupDisplayNames[highestBuild.second],
                )
            )
        }

        return Aggregate(
            prefix =
                MiniMessage.miniMessage().deserialize(prefix),
            suffix =
                MiniMessage.miniMessage().deserialize(suffix),
            groups = groupComponent.build(),
        )
    }

    private fun groupTextComponent(
        group: Pair<Int, String>,
        hoverName: String?,
        displayName: String?,
    ): Component {
        val groupName = group.second

        return MiniMessage.miniMessage()
            .deserialize(displayName ?: groupName)
            .also {
                if (!hoverName.isNullOrEmpty()) {
                    it.hoverEvent(
                        HoverEvent.showText(Component.text(hoverName)),
                    )
                }
            }
    }

    private fun buildGroupList(groupDisplayPriorities: Map<ChatGroupType, List<String>>) {
        val trustedGroupPriority = groupDisplayPriorities[ChatGroupType.TRUST]
        val builderGroupPriority = groupDisplayPriorities[ChatGroupType.BUILD]
        val donorGroupPriority = groupDisplayPriorities[ChatGroupType.DONOR]

        trustedGroupPriority?.withIndex()?.forEach {
            groupPriorities[it.value.lowercase()] = Pair(ChatGroupType.TRUST, it.index)
        }
        builderGroupPriority?.withIndex()?.forEach {
            groupPriorities[it.value.lowercase()] = Pair(ChatGroupType.BUILD, it.index)
        }
        donorGroupPriority?.withIndex()?.forEach {
            groupPriorities[it.value.lowercase()] = Pair(ChatGroupType.DONOR, it.index)
        }
    }
}
