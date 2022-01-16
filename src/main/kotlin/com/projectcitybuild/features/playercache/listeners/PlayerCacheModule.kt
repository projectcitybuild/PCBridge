package com.projectcitybuild.features.playercache

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.playercache.listeners.PlayerCacheListener
import net.md_5.bungee.api.plugin.Listener
import javax.inject.Inject

class PlayerCacheModule @Inject constructor(
    playerCacheListener: PlayerCacheListener,
): BungeecordFeatureModule {

    override val bungeecordListeners: Array<Listener> = arrayOf(
        playerCacheListener,
    )
}