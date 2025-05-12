package com.projectcitybuild.pcbridge.paper.architecture.chat.listeners

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatDecoratorChain
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatSender
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Only one event listener per plugin can be the designated
 * [ChatRenderer] delegate.
 *
 * Therefore, we'll funnel everything through this listener,
 * but let every feature module have a say by enabling them to
 * register a [ChatMiddleware] that's called as part of a chain
 */
class AsyncChatListener(
    private val decorators: ChatDecoratorChain,
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
            val chatSender = decorators.pipe(
                ChatSender(source, sourceDisplayName)
            )
            val chatMessage = decorators.pipe(
                ChatMessage(source, message)
            )
            Component.text().run {
                append(
                    chatSender.sourceDisplayName,
                    Component.text(": "),
                    chatMessage.message,
                )
                build()
            }
        }
}
