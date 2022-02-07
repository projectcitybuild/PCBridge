package com.projectcitybuild.features.afk

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.afk.commands.AFKCommand
import com.projectcitybuild.features.afk.listeners.AFKListener
import com.projectcitybuild.features.afk.listeners.IncomingAFKEndListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class AFKModule {

    class Bungeecord @Inject constructor(
        afkCommand: AFKCommand,
        incomingAFKEndListener: IncomingAFKEndListener,
    ): BungeecordFeatureModule {

        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            afkCommand,
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            incomingAFKEndListener,
        )
    }

    class Spigot @Inject constructor(
        afkListener: AFKListener,
    ): SpigotFeatureModule {

        override val spigotListeners: Array<SpigotListener> = arrayOf(
            afkListener,
        )
    }
}