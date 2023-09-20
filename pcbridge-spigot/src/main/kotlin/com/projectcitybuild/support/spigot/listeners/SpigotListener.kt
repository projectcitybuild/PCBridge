package com.projectcitybuild.support.spigot.listeners

import org.bukkit.event.Event
import org.bukkit.event.Listener

@Deprecated("Use EventPipeline instead")
interface SpigotListener<T: Event>: Listener {
    suspend fun handle(event: T)
}
