package com.projectcitybuild.modules.eventbroadcast

import org.bukkit.event.Event

interface LocalEventBroadcaster {
    fun emit(event: Event)
}
