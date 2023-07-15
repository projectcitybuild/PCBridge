package com.projectcitybuild.modules.chat

import com.projectcitybuild.Permissions
import com.projectcitybuild.modules.chat.commands.BadgeCommand
import com.projectcitybuild.modules.chat.listeners.AsyncPlayerChatListener
import com.projectcitybuild.modules.chat.listeners.EmojiChatListener
import com.projectcitybuild.modules.chat.listeners.SyncBadgesOnJoinListener
import com.projectcitybuild.support.commandapi.ToggleOption
import com.projectcitybuild.support.modules.ModuleDeclaration
import com.projectcitybuild.support.modules.PluginModule
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor

class ChatModule: PluginModule {
    override fun register(module: ModuleDeclaration) {
        module {
            command("badge") {
                withPermission(Permissions.COMMAND_CHAT_TOGGLE_BADGE)
                withShortDescription("Shows or hides your chat badge")
                withOptionalArguments(
                    MultiLiteralArgument("toggle", listOf("on", "off"))
                )
                executesPlayer(PlayerCommandExecutor { player, args ->
                    val desiredState = when(args.get("toggle")) {
                        "on" -> ToggleOption.ON
                        "off" -> ToggleOption.OFF
                        else -> ToggleOption.UNSPECIFIED
                    }
                    BadgeCommand(
                        container.playerConfigRepository,
                    ).execute(player, desiredState)
                })
            }

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