package com.projectcitybuild.pcbridge.paper.features.borders

import com.projectcitybuild.pcbridge.paper.features.borders.data.Border
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Maintains a set of players for a world that need to be
 * constantly movement checked for border collision, due to proximity
 * to a border
 */
class BorderCollidablePlayers(
    private val border: Border,
) {
    private val monitoredPlayers: MutableSet<UUID> = mutableSetOf()

    fun watch(player: Player) {
        if (!border.contains(player.location)) {
            // TODO
            return
        }

        // Determine when to next check based on proximity to border
        
    }

    fun nearBorder(player: Player): Boolean {
        return monitoredPlayers.contains(player.uniqueId)
    }
}