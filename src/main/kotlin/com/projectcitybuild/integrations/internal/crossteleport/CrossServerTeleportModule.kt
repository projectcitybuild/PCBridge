package com.projectcitybuild.integrations.internal.crossteleport

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.integrations.internal.crossteleport.listeners.TeleportOnJoinListener
import javax.inject.Inject

class CrossServerTeleportModule @Inject constructor(
    teleportOnJoinListener: TeleportOnJoinListener,
) : SpigotFeatureModule {

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        teleportOnJoinListener,
    )
}
