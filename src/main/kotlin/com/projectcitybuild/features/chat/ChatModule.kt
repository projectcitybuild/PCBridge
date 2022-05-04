package com.projectcitybuild.features.chat

import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.chat.commands.ACommand
import com.projectcitybuild.features.chat.commands.IgnoreCommand
import com.projectcitybuild.features.chat.commands.MuteCommand
import com.projectcitybuild.features.chat.commands.ReplyCommand
import com.projectcitybuild.features.chat.commands.UnignoreCommand
import com.projectcitybuild.features.chat.commands.UnmuteCommand
import com.projectcitybuild.features.chat.commands.WhisperCommand
import com.projectcitybuild.features.chat.listeners.ChatListener
import com.projectcitybuild.plugin.environment.SpigotCommand
import javax.inject.Inject
import org.bukkit.event.Listener as SpigotListener

class ChatModule @Inject constructor(
    aCommand: ACommand,
    chatListener: ChatListener,
    ignoreCommand: IgnoreCommand,
    muteCommand: MuteCommand,
    replyCommand: ReplyCommand,
    unignoreCommand: UnignoreCommand,
    unmuteCommand: UnmuteCommand,
    whisperCommand: WhisperCommand,
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        aCommand,
        ignoreCommand,
        muteCommand,
        replyCommand,
        unignoreCommand,
        unmuteCommand,
        whisperCommand,
    )

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        chatListener,
    )
}
