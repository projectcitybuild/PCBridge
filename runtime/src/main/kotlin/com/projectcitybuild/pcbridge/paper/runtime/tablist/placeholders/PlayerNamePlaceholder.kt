package com.projectcitybuild.pcbridge.paper.runtime.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.runtime.tablist.TabPlaceholder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PlayerNamePlaceholder : TabPlaceholder {
    override val placeholder: String = "name"

    override suspend fun value(player: Player): Component = player.displayName()
}
