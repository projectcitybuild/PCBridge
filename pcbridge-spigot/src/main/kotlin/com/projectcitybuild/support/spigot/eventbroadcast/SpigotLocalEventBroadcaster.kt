package com.projectcitybuild.support.spigot.eventbroadcast

import com.github.shynixn.mccoroutine.bukkit.callSuspendingEvent
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin

class SpigotLocalEventBroadcaster(
    private val plugin: JavaPlugin,
): LocalEventBroadcaster {

    override fun emit(event: BroadcastableEvent) {
        if (event !is Event) {
            throw Exception("Cannot cast event to Spigot Event [$event]")
        }
        Bukkit.getPluginManager().callSuspendingEvent(event, plugin)
    }
}
