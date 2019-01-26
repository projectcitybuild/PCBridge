package com.projectcitybuild.core.contracts

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Represents an event handler (for events other than commands).
 *
 * For example, when a player connects to the server, the connection event
 * can be observed by conforming to the Listenable interface and then
 * registering it in a ListenerDelegatable.
 */
interface Listenable<in EventType: Event> : Listener, Injectable {
    @EventHandler
    fun observe(event: EventType)
}