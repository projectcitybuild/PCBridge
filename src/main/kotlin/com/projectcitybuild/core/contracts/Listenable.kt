package com.projectcitybuild.core.contracts

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

interface Listenable<in EventType: Event> : Listener, Injectable {
    @EventHandler
    fun observe(event: EventType)
}