package com.projectcitybuild.pcbridge.paper.features.chat.listeners

import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.paper.features.chat.repositories.ChatGroupRepository
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class FormatNameChatListener(
    private val chatBadgeRepository: ChatBadgeRepository,
    private val chatGroupRepository: ChatGroupRepository,
) : Listener, ChatRenderer.ViewerUnaware {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncChatEvent) {
        event.renderer(
            ChatRenderer.viewerUnaware(this),
        )
    }

    override fun render(
        source: Player,
        sourceDisplayName: Component,
        message: Component,
    ): Component =
        runBlocking {
            val uuid = source.uniqueId
            val badge = chatBadgeRepository.getComponent(uuid)
            val groups = chatGroupRepository.getGroupsComponent(uuid)

            Component.text().run {
                if (badge.value != null) {
                    append(badge.value).appendSpace()
                }
                if (groups.value != null) {
                    append(groups.value).appendSpace()
                }
                append(
                    sourceDisplayName,
                    Component.text(": "),
                    urlClickable(message),
                )
                build()
            }
        }

    private fun urlClickable(message: Component): Component {
        // Only the legacy serializer automatically converts URLs to clickable text
        val legacySerializer = LegacyComponentSerializer
            .builder()
            .extractUrls()
            .build()

        val withUrls = legacySerializer.serialize(message)

        return legacySerializer.deserialize(withUrls)
    }
}
