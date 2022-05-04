package com.projectcitybuild.features.teleporthistory

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.teleporthistory.commands.BackCommand
import com.projectcitybuild.features.teleporthistory.listeners.PlayerPreTeleportListener
import com.projectcitybuild.plugin.environment.SpigotCommand
import javax.inject.Inject

class TeleportHistoryModule @Inject constructor(
    backCommand: BackCommand,
    playerPreTeleportListener: PlayerPreTeleportListener,
) : SpigotFeatureModule {

    override val spigotCommands: Array<SpigotCommand> = arrayOf(
        backCommand,
    )

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        playerPreTeleportListener,
    )
}
