package com.projectcitybuild.pcbridge.paper.features.groups.decorators

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabPlaceholder
import com.projectcitybuild.pcbridge.paper.features.groups.RolesFilter
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class TabGroupPlaceholder(
    private val rolesFilter: RolesFilter,
    private val store: Store,
): TabPlaceholder {
    override val placeholder: String = "%groups%"

    override suspend fun value(player: Player): Component {
        val playerState = store.state.players[player.uniqueId]
        val roles = rolesFilter.filter(playerState?.groups?.toSet() ?: emptySet())
        val roleNames = roles.values.mapNotNull { it.minecraftName }

        return Component.text(
            if (playerState == null) "Unknown"
            else if (roleNames.isEmpty()) "Guest"
            else roleNames.joinToString(separator = ", ")
        )
    }
}