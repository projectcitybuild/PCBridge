package com.projectcitybuild.features.chat

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.support.textcomponent.add
import com.projectcitybuild.support.textcomponent.addIf
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.util.UUID
import javax.inject.Inject

class ChatBadgeFormatter @Inject constructor(
    private val chatBadgeRepository: ChatBadgeRepository,
    private val config: Config,
) {
    fun get(playerUUID: UUID): TextComponent? {
        val badges = chatBadgeRepository.get(playerUUID)
        if (badges.isEmpty()) {
            return null
        }

        val formattedBadges = badges.withIndex().map { (index, badge) ->
            TextComponent()
                .add(badge.unicodeIcon)
                .add(" ") { it.color = ChatColor.WHITE }
                .add(badge.displayName)
                .addIf(index < badges.size - 1, "\n")
                .toLegacyText()
        }

        val tc = TextComponent()
        TextComponent
            .fromLegacyText(config.get(ConfigKeys.chatBadgeIcon))
            .forEach { c ->
                tc.add(c) {
                    it.hoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text(formattedBadges.joinToString(separator = ""))
                    )
                }
            }

        tc.add(" ") {
            it.color = ChatColor.RESET
        }
        return tc
    }
}
