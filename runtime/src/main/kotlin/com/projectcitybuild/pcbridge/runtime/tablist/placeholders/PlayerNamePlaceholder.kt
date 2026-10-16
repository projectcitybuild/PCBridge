package com.projectcitybuild.pcbridge.runtime.tablist.placeholders

import com.projectcitybuild.pcbridge.runtime.tablist.TabPlaceholder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PlayerNamePlaceholder : TabPlaceholder {
    override val placeholder: String = "name"

    override suspend fun value(player: Player): Component = player.displayName()
}
