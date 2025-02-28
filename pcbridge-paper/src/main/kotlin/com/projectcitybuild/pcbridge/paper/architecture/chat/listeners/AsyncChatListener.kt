package com.projectcitybuild.pcbridge.paper.architecture.chat.listeners

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.Chat
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddlewareChain
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class AsyncChatListener(
    private val middlewareChain: ChatMiddlewareChain,
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
            val chat = middlewareChain.pipe(
                Chat(source, sourceDisplayName, message)
            )
            Component.text().run {
                append(
                    chat.sourceDisplayName,
                    Component.text(": "),
                    chat.message,
                )
                build()
            }
        }
}
