package com.projectcitybuild.modules.proxyadapter.broadcast

import com.projectcitybuild.modules.proxyadapter.messages.TextComponentBox

interface MessageBroadcaster {
    fun broadcastToAll(message: TextComponentBox)
}
