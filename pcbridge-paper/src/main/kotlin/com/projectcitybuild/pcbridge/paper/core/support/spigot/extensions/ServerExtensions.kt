package com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server

fun Server.broadcastRich(message: String)
    = broadcast(MiniMessage.miniMessage().deserialize(message))