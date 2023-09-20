package com.projectcitybuild.modules.chat

import com.projectcitybuild.modules.chat.listeners.AsyncPlayerChatListener
import com.projectcitybuild.pcbridge.core.architecture.events.EventPipeline
import com.projectcitybuild.pcbridge.core.architecture.features.PluginFeature
import org.bukkit.event.player.AsyncPlayerChatEvent
import kotlin.coroutines.CoroutineContext

class ChatFeature(
    eventPipeline: EventPipeline,
    contextBuilder: () -> CoroutineContext,
    private val asyncPlayerChatListener: AsyncPlayerChatListener,
): PluginFeature(eventPipeline, contextBuilder) {

    override fun onLoad() {
        events.subscribe(AsyncPlayerChatEvent::class.java) { event ->
            asyncPlayerChatListener.handle(event)
        }
        // listener(EmojiChatListener())
        // listener(
        //     SyncBadgesOnJoinListener(
        //         container.chatBadgeRepository,
        //     )
        // )

        // CommandAPICommand("badge").apply {
        //     withPermission(Permissions.COMMAND_CHAT_TOGGLE_BADGE)
        //     withShortDescription("Shows or hides your chat badge")
        //     withOptionalArguments(
        //         MultiLiteralArgument("toggle", listOf("on", "off"))
        //     )
        //     executesPlayer(PlayerCommandExecutor { player, args ->
        //         val desiredState = when(args.get("toggle")) {
        //             "on" -> ToggleOption.ON
        //             "off" -> ToggleOption.OFF
        //             else -> ToggleOption.UNSPECIFIED
        //         }
        //         BadgeCommand(
        //             container.playerConfigRepository,
        //         ).execute(player, desiredState)
        //     })
        //     register()
        // }
    }
}