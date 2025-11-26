package com.projectcitybuild.pcbridge.paper.core.support.component

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage

fun Audience.sendMessageRich(message: String)
    = sendMessage(MiniMessage.miniMessage().deserialize(message))