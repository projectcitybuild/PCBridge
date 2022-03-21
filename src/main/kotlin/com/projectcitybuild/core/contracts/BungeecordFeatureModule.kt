package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand

interface BungeecordFeatureModule {

    val bungeecordCommands: Array<BungeecordCommand>
        get() = emptyArray()

    val bungeecordListeners: Array<BungeecordListener>
        get() = emptyArray()

    val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener>
        get() = emptyArray()
}