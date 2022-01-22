package com.projectcitybuild.features.teleporthistory

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.teleporthistory.listeners.PlayerSummonListener
import com.projectcitybuild.features.teleporthistory.listeners.PlayerTeleportListener
import com.projectcitybuild.features.teleporthistory.listeners.PlayerWarpListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import javax.inject.Inject

class TeleportHistoryModule {

    class Bungeecord @Inject constructor(
    ): BungeecordFeatureModule {
        override val bungeecordCommands: Array<BungeecordCommand> = arrayOf(
        )
    }

    class Spigot @Inject constructor(
        playerTeleportListener: PlayerTeleportListener,
        playerSummonListener: PlayerSummonListener,
        playerWarpListener: PlayerWarpListener,
    ): SpigotFeatureModule {
        override val spigotListeners: Array<SpigotListener> = arrayOf(
            playerTeleportListener,
            playerSummonListener,
            playerWarpListener,
        )
    }
}