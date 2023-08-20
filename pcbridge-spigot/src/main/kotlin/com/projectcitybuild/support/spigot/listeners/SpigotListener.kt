package com.projectcitybuild.support.spigot.listeners

import org.bukkit.event.Event
import org.bukkit.event.Listener

interface SpigotListener<T: Event>: Listener {
    suspend fun handle(event: T)
}
