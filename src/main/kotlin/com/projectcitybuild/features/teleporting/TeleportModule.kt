package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.teleporting.commands.TPCommand
import com.projectcitybuild.features.teleporting.commands.TPHereCommand
import com.projectcitybuild.features.teleporting.commands.TPOCommand
import com.projectcitybuild.features.teleporting.commands.TPOHereCommand
import com.projectcitybuild.features.teleporting.subchannels.AwaitJoinTeleportChannelListener
import com.projectcitybuild.features.teleporting.subchannels.ImmediateTeleportChannelListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class TeleportModule {

    class Bungeecord @Inject constructor(
        tpCommand: TPCommand,
        tpHereCommand: TPHereCommand,
        tpoCommand: TPOCommand,
        tpoHereCommand: TPOHereCommand,
        tpToggleCommand: TPHereCommand,
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            tpCommand,
            tpHereCommand,
            tpoCommand,
            tpoHereCommand,
            tpToggleCommand,
        )
    }

    class Spigot @Inject constructor(
        awaitJoinTeleportChannelListener: AwaitJoinTeleportChannelListener,
        immediateTeleportChannelListener: ImmediateTeleportChannelListener,
    ): SpigotFeatureModule {
        override val spigotSubChannelListeners: Array<SpigotSubChannelListener> = arrayOf(
            awaitJoinTeleportChannelListener,
            immediateTeleportChannelListener,
        )
    }
}