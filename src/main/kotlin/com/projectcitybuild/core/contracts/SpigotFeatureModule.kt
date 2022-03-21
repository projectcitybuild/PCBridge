package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand

interface SpigotFeatureModule {

    val spigotCommands: Array<SpigotCommand>
        get() = emptyArray()

    val spigotListeners: Array<SpigotListener>
        get() = emptyArray()

    val spigotSubChannelListeners: Array<SpigotSubChannelListener>
        get() = emptyArray()

    fun onEnable() = run { }
    fun onDisable() = run { }
}
