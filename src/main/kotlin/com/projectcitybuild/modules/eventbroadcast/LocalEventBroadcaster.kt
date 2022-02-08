package com.projectcitybuild.modules.eventbroadcast

import org.bukkit.event.Event

interface LocalEventBroadcaster {

    /**
     * Broadcasts an event to all listeners on the current server
     *
     * @param event The event to broadcast
     */
    fun emit(event: Event)
}
