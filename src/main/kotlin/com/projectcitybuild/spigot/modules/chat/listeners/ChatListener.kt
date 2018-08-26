package com.projectcitybuild.spigot.modules.chat.listeners

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.contracts.Listenable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listenable<AsyncPlayerChatEvent> {
    override var environment: Environment? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerChatEvent) {
        val sendingPlayer = environment!!.get(event.player.uniqueId)
        if (sendingPlayer?.isMuted == true) {
            event.isCancelled = true
            event.player.sendMessage("You cannot chat while muted")
            return
        }
    }

}