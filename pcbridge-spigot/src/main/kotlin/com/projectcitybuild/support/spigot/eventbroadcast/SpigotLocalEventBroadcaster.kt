package com.projectcitybuild.support.spigot.eventbroadcast

import com.projectcitybuild.pcbridge.core.contracts.PlatformScheduler
import org.bukkit.Bukkit
import org.bukkit.event.Event

class SpigotLocalEventBroadcaster(
    private val scheduler: PlatformScheduler,
): LocalEventBroadcaster {

    override fun emit(event: BroadcastableEvent) {
        if (event !is Event) {
            throw Exception("Cannot cast event to Spigot Event [$event]")
        }
        // TODO: refactor this to not be blocking
        scheduler.sync {
            Bukkit.getPluginManager().callEvent(event)
        }
    }
}
