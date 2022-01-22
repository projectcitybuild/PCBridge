package com.projectcitybuild.features.warps

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.warps.commands.DelWarpCommand
import com.projectcitybuild.features.warps.commands.SetWarpCommand
import com.projectcitybuild.features.warps.commands.WarpCommand
import com.projectcitybuild.features.warps.commands.WarpsCommand
import com.projectcitybuild.features.warps.listeners.WarpOnJoinListener
import com.projectcitybuild.features.warps.subchannels.AcrossServerWarpChannelListener
import com.projectcitybuild.features.warps.subchannels.SameServerWarpChannelListener
import com.projectcitybuild.features.warps.subchannels.IncomingSetWarpListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class WarpModule {

    class Bungeecord @Inject constructor(
        delWarpCommand: DelWarpCommand,
        warpCommand: WarpCommand,
        warpsCommand: WarpsCommand,
        incomingSetWarpListener: IncomingSetWarpListener,
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
            delWarpCommand,
            warpCommand,
            warpsCommand,
        )

        override val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener> = arrayOf(
            incomingSetWarpListener,
        )
    }

    class Spigot @Inject constructor(
        setWarpCommand: SetWarpCommand,
        sameServerWarpChannelListener: SameServerWarpChannelListener,
        acrossServerWarpChannelListener: AcrossServerWarpChannelListener,
        warpOnJoinListener: WarpOnJoinListener,
    ): SpigotFeatureModule {
        override val spigotCommands: Array<SpigotCommand> = arrayOf(
            setWarpCommand,
        )

        override val spigotListeners: Array<SpigotListener> = arrayOf(
            warpOnJoinListener,
        )

        override val spigotSubChannelListeners: Array<SpigotSubChannelListener> = arrayOf(
            acrossServerWarpChannelListener,
            sameServerWarpChannelListener,
        )
    }
}