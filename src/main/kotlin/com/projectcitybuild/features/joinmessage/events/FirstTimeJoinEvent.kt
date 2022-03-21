package com.projectcitybuild.features.joinmessage.events

import com.projectcitybuild.modules.eventbroadcast.BroadcastableEvent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Event as BungeecordEvent

class FirstTimeJoinEvent(val player: ProxiedPlayer) : BungeecordEvent(), BroadcastableEvent
