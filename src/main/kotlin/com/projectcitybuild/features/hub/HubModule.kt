package com.projectcitybuild.features.hub

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.hub.commands.HubCommand
import com.projectcitybuild.features.hub.commands.SetHubCommand
import com.projectcitybuild.features.hub.subchannels.IncomingSetHubListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class HubModule {

    class Bungeecord @Inject constructor(
        hubCommand: HubCommand,
        incomingSetHubListener: IncomingSetHubListener,
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            hubCommand,
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            incomingSetHubListener,
        )
    }

    class Spigot @Inject constructor(
        setHubCommand: SetHubCommand
    ): SpigotFeatureModule {
        override val spigotCommands: Array<SpigotCommand> = arrayOf(
            setHubCommand,
        )
    }
}