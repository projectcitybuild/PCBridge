package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.plugin.environment.SpigotCommand

interface SpigotFeatureModule {

    val spigotCommands: Array<SpigotCommand>
        get() = emptyArray()

    val spigotListeners: Array<SpigotListener>
        get() = emptyArray()

    fun onEnable() = run { }
    fun onDisable() = run { }
}
