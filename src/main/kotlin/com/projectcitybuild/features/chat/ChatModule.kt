package com.projectcitybuild.features.chat

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.chat.commands.*
import com.projectcitybuild.features.chat.listeners.ChatListener
import com.projectcitybuild.features.chat.subchannels.IncomingChatChannelListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject
import org.bukkit.event.Listener as SpigotListener

class ChatModule {

    class Bungeecord @Inject constructor(
        aCommand: ACommand,
        ignoreCommand: IgnoreCommand,
        muteCommand: MuteCommand,
        replyCommand: ReplyCommand,
        unignoreCommand: UnignoreCommand,
        unmuteCommand: UnmuteCommand,
        whisperCommand: WhisperCommand,
        incomingChatChannelListener: IncomingChatChannelListener,
    ) : BungeecordFeatureModule {

        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            aCommand,
            ignoreCommand,
            muteCommand,
            replyCommand,
            unignoreCommand,
            unmuteCommand,
            whisperCommand,
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            incomingChatChannelListener,
        )
    }

    class Spigot @Inject constructor(
        chatListener: ChatListener
    ) : SpigotFeatureModule {

        override val spigotListeners: Array<SpigotListener> = arrayOf(
            chatListener,
        )
    }
}
