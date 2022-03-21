package com.projectcitybuild.wiring.shared.crossteleport

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.wiring.shared.crossteleport.listeners.TeleportOnJoinListener
import javax.inject.Inject

class CrossTeleportModule @Inject constructor(
    teleportOnJoinListener: TeleportOnJoinListener,
): SpigotFeatureModule {

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        teleportOnJoinListener,
    )
}