package com.projectcitybuild.features.playercache

import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.features.playercache.listeners.PlayerCacheListener
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import net.md_5.bungee.api.plugin.Listener

class PlayerCacheModule(
    playerConfigCache: PlayerConfigCache,
    playerConfigRepository: PlayerConfigRepository,
): BungeecordFeatureModule {

    override val bungeecordListeners: Array<Listener> = arrayOf(
        PlayerCacheListener(playerConfigCache, playerConfigRepository),
    )
}