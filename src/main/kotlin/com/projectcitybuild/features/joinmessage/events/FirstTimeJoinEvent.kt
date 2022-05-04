package com.projectcitybuild.features.joinmessage.events

import com.projectcitybuild.modules.eventbroadcast.BroadcastableEvent
import org.bukkit.entity.Player
import net.md_5.bungee.api.plugin.Event as BungeecordEvent

class FirstTimeJoinEvent(val player: Player) : BungeecordEvent(), BroadcastableEvent
