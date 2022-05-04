package com.projectcitybuild.shared.playercache

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.shared.playercache.listeners.PlayerCacheListener
import javax.inject.Inject

class PlayerCacheModule @Inject constructor(
    playerCacheListener: PlayerCacheListener,
) : SpigotFeatureModule {

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        playerCacheListener,
    )
}
