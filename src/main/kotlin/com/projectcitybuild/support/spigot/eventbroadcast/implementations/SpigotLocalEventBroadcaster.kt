package com.projectcitybuild.support.spigot.eventbroadcast.implementations

import com.projectcitybuild.support.spigot.eventbroadcast.BroadcastableEvent
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import org.bukkit.Bukkit
import org.bukkit.event.Event
import javax.inject.Inject

class SpigotLocalEventBroadcaster @Inject constructor() : LocalEventBroadcaster {

    override fun emit(event: BroadcastableEvent) {
        if (event !is Event) {
            throw Exception("Cannot cast event to Spigot Event [$event]")
        }
        Bukkit.getPluginManager().callEvent(event)
    }
}
