package com.projectcitybuild.support.spigot.listeners

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

interface SpigotListener<T: Event>: Listener {
    val priority: EventPriority
        get() = EventPriority.NORMAL

    suspend fun handle(event: T)
}
