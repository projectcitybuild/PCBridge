package com.projectcitybuild.pcbridge.paper.features.opelevate.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.features.opelevate.opElevateTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class VanillaOpInterceptListener: Listener {
    @EventHandler
    fun onPlayerCommandPreprocess(
        event: PlayerCommandPreprocessEvent,
    ) = event.scopedSync(opElevateTracer, this::class.java) {
        // Event only triggered by players. No further checks needed - we still
        // want to allow console to /op players in emergencies
        val message = event.message.trimEnd().lowercase()
        if (message == "/op" || message.startsWith("/op ") ||
            message == "/deop" || message.startsWith("/deop ")
        ) {
            event.isCancelled = true
        }
    }
}