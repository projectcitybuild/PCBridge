package com.projectcitybuild.core.contracts

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand

interface BungeecordFeatureModule {

    val bungeecordCommands: Array<BungeecordCommand>
        get() = emptyArray()

    val bungeecordListeners: Array<BungeecordListener>
        get() = emptyArray()

    val bungeecordSubChannelListeners: Array<BungeecordSubChannelListener>
        get() = emptyArray()
}

interface SpigotFeatureModule {

    val spigotCommands: Array<SpigotCommand>
        get() = emptyArray()

    val spigotListeners: Array<SpigotListener>
        get() = emptyArray()

    val spigotSubChannelListeners: Array<SpigotSubChannelListener>
        get() = emptyArray()
}