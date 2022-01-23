package com.projectcitybuild.features.joinmessage

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.BungeecordFeatureModule
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.joinmessage.listeners.NetworkJoinMessageListener
import com.projectcitybuild.features.joinmessage.listeners.SupressJoinMessageListener
import com.projectcitybuild.features.joinmessage.listeners.WelcomeMessageListener
import javax.inject.Inject

class JoinMessageModule {

    class Bungeecord @Inject constructor(
        networkJoinMessageListener: NetworkJoinMessageListener,
        welcomeMessageListener: WelcomeMessageListener,
    ): BungeecordFeatureModule {
        override val bungeecordListeners: Array<BungeecordListener> = arrayOf(
            networkJoinMessageListener,
            welcomeMessageListener,
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