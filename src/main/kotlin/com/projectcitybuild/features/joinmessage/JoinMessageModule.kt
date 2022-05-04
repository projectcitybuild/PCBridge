package com.projectcitybuild.features.joinmessage

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.core.contracts.SpigotFeatureModule
import com.projectcitybuild.features.joinmessage.listeners.FirstTimeJoinMessageListener
import com.projectcitybuild.features.joinmessage.listeners.ServerJoinMessageListener
import com.projectcitybuild.features.joinmessage.listeners.WelcomeMessageListener
import javax.inject.Inject

class JoinMessageModule @Inject constructor(
    serverJoinMessageListener: ServerJoinMessageListener,
    welcomeMessageListener: WelcomeMessageListener,
    firstTimeJoinMessageListener: FirstTimeJoinMessageListener,
) : SpigotFeatureModule {

    override val spigotListeners: Array<SpigotListener> = arrayOf(
        serverJoinMessageListener,
        welcomeMessageListener,
        firstTimeJoinMessageListener,
    )
}
