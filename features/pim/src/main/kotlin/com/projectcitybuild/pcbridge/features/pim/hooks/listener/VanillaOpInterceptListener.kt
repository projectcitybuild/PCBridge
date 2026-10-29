package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class VanillaOpInterceptListener : Listener {
    /**
     * Intercepts /op /deop usage by players and cancels them
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) = event.tracedSync(pimTracer, this::class.java) {
        // Event only triggered by players. No further checks needed - we still
        // want to allow console to /op players in emergencies
        val label = event.message
            .trim()
            .removePrefix("/")
            .split(Regex("\\s+"), limit = 2)
            .first()
            .lowercase()

        val namespace = label.substringBefore(':').takeIf { ':' in label }
        val command = label.substringAfterLast(':')
        val isVanillaCommand = namespace == null || namespace == "minecraft" || namespace == "bukkit"
        if (isVanillaCommand && (command == "op" || command == "deop")) {
            event.isCancelled = true
        }
    }
}
