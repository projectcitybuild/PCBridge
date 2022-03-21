package com.projectcitybuild.features.teleporting

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.teleporting.commands.*
import com.projectcitybuild.features.teleporting.subchannels.AcrossServerTeleportChannelListener
import com.projectcitybuild.features.teleporting.subchannels.SameServerTeleportChannelListener
import com.projectcitybuild.features.teleporting.subchannels.SwitchPlayerServerSubChannelListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class TeleportModule {

    class Bungeecord @Inject constructor(
        tpCommand: TPCommand,
        tpHereCommand: TPHereCommand,
        tpaCommand: TPACommand,
        tpaHereCommand: TPAHereCommand,
        tpAcceptCommand: TPAcceptCommand,
        tpDenyCommand: TPDenyCommand,
        tpoCommand: TPOCommand,
        tpoHereCommand: TPOHereCommand,
        tpToggleCommand: TPToggleCommand,
        switchPlayerServerSubChannelListener: SwitchPlayerServerSubChannelListener,
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            tpCommand,
            tpHereCommand,
            tpaCommand,
            tpaHereCommand,
            tpAcceptCommand,
            tpDenyCommand,
            tpoCommand,
            tpoHereCommand,
            tpToggleCommand,
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            switchPlayerServerSubChannelListener,
        )
    }

    class Spigot @Inject constructor(
        acrossServerTeleportChannelListener: AcrossServerTeleportChannelListener,
        sameServerTeleportChannelListener: SameServerTeleportChannelListener,
    ): SpigotFeatureModule {
        override val spigotSubChannelListeners: Array<SpigotSubChannelListener> = arrayOf(
            acrossServerTeleportChannelListener,
            sameServerTeleportChannelListener,
        )
    }
}