package com.projectcitybuild.features.joinmessage

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.joinmessage.listeners.NetworkJoinMessageListener
import com.projectcitybuild.features.joinmessage.listeners.SupressJoinMessageListener
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener
import javax.inject.Inject

class JoinMessageModule {

    class Bungeecord @Inject constructor(
        networkJoinMessageListener: NetworkJoinMessageListener,
    ): BungeecordFeatureModule {
        override val bungeecordListeners: Array<BungeecordListener> = arrayOf(
            networkJoinMessageListener,
        )
    }

    class Spigot @Inject constructor(
        supressJoinMessageListener: SupressJoinMessageListener,
    ): SpigotFeatureModule {
        override val spigotListeners: Array<SpigotListener> = arrayOf(
            supressJoinMessageListener,
        )
    }
}