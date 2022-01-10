package com.projectcitybuild.core.contracts

import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import org.bukkit.event.Listener as SpigotListener
import net.md_5.bungee.api.plugin.Listener as BungeecordListener

interface BungeecordFeatureModule {

    val bungeecordCommands: Array<BungeecordCommand>
        get() = emptyArray()

    val bungeecordListeners: Array<BungeecordListener>
        get() = emptyArray()
}

interface SpigotFeatureModule {

    val spigotCommands: Array<Commandable>
        get() = emptyArray()

    val spigotListeners: Array<SpigotListener>
        get() = emptyArray()
}