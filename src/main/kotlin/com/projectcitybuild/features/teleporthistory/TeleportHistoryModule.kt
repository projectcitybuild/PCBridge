package com.projectcitybuild.features.teleporthistory

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.teleporthistory.commands.BackCommand
import com.projectcitybuild.features.teleporthistory.listeners.PlayerSummonListener
import com.projectcitybuild.features.teleporthistory.listeners.PlayerTeleportListener
import com.projectcitybuild.features.teleporthistory.listeners.PlayerWarpListener
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import javax.inject.Inject

class TeleportHistoryModule {

    class Spigot @Inject constructor(
        backCommand: BackCommand,
        playerTeleportListener: PlayerTeleportListener,
        playerSummonListener: PlayerSummonListener,
        playerWarpListener: PlayerWarpListener,
    ) : SpigotFeatureModule {

        override val spigotCommands: Array<SpigotCommand> = arrayOf(
            backCommand,
        )

        override val spigotListeners: Array<SpigotListener> = arrayOf(
            playerTeleportListener,
            playerSummonListener,
            playerWarpListener,
        )
    }
}
