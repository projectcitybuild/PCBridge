package com.projectcitybuild.pcbridge.paper.features.chat

import com.projectcitybuild.pcbridge.http.models.Badge
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatBadgeFormatter() {
    fun format(badges: List<Badge>, icon: String): Component {
        if (badges.isEmpty()) {
            return Component.empty()
        }

        val formattedBadge =
            Component.text().append(
                Component.text("Badges")
                    .color(NamedTextColor.YELLOW)
                    .appendNewline(),
                Component.text("---")
                    .color(NamedTextColor.GRAY)
                    .appendNewline(),
            )
        badges.withIndex().forEach { (index, badge) ->
            formattedBadge.append(
                Component.text(badge.unicodeIcon),
                Component.space(),
                Component.text(badge.displayName),
            ).also {
                if (index < badges.size - 1) {
                    it.appendNewline()
                }
            }
        }
        return MiniMessage.miniMessage()
            .deserialize(icon)
            .hoverEvent(HoverEvent.showText(formattedBadge))
    }
}
