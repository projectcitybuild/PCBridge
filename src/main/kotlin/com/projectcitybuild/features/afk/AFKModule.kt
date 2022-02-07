package com.projectcitybuild.features.afk

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.afk.commands.AFKCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class AFKModule {

    class Bungeecord @Inject constructor(
        afkCommand: AFKCommand,
    ): BungeecordFeatureModule {

        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            afkCommand,
        )

//        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
//            incomingChatChannelListener,
//        )
    }

    class Spigot @Inject constructor(
//        chatListener: ChatListener
    ): SpigotFeatureModule {

//        override val spigotListeners: Array<SpigotListener> = arrayOf(
//            chatListener,
//        )
    }
}