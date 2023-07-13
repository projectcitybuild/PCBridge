package com.projectcitybuild.modules.chat

import com.projectcitybuild.modules.chat.actions.ToggleBadge
import com.projectcitybuild.modules.chat.commands.BadgeCommand
import com.projectcitybuild.modules.chat.listeners.AsyncPlayerChatListener
import com.projectcitybuild.modules.chat.listeners.EmojiChatListener
import com.projectcitybuild.modules.chat.listeners.SyncBadgesOnJoinListener
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule

class ChatModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command(
                BadgeCommand(
                    ToggleBadge(container.playerConfigRepository),
                ),
            )
            listener(
                AsyncPlayerChatListener(
                    container.server,
                    container.playerConfigRepository,
                    container.chatGroupFormatter,
                    container.chatBadgeFormatter,
                ),
            )
            listener(EmojiChatListener())
            listener(
                SyncBadgesOnJoinListener(
                    container.chatBadgeRepository,
                )
            )
        }
    }
}