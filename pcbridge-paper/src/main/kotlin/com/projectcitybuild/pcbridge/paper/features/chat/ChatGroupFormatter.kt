package com.projectcitybuild.pcbridge.paper.features.chat

import com.projectcitybuild.pcbridge.http.pcb.models.Group
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatGroupFormatter {
    fun format(groups: Set<Group>): Component? {
        if (groups.isEmpty()) {
            return null
        }
        val mapping = mutableMapOf<String, Group>()
        for (group in groups) {
            val groupType = group.groupType
            val displayPriority = group.displayPriority

            if (groupType == null || displayPriority == null) continue;

            val existing = mapping[groupType]
            if (existing == null || existing.displayPriority!! < displayPriority) {
                mapping[groupType] = group
            }
        }
        return Component.text().run {
            mapping["donor"]?.let { append(it::component) }
            mapping["staff"]?.let { append(it::component) }
            mapping["trust"]?.let { append(it::component) }
            mapping["build"]?.let { append(it::component) }
            build()
        }
    }
}

private fun Group.component() = MiniMessage.miniMessage()
    .deserialize(displayName ?: name)
    .also {
        if (hoverText.isNullOrEmpty()) {
            return it
        }
        return it.hoverEvent(
            HoverEvent.showText(Component.text(hoverText!!)),
        )
    }
