package com.projectcitybuild.modules.eventbroadcast

import org.bukkit.Bukkit
import org.bukkit.event.Event
import javax.inject.Inject

class SpigotLocalEventBroadcaster @Inject constructor(): LocalEventBroadcaster {

    override fun emit(event: Event) {
        Bukkit.getPluginManager().callEvent(event)
    }
}