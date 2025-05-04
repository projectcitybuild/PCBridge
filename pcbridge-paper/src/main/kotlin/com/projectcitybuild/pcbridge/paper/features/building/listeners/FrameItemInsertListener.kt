package com.projectcitybuild.pcbridge.paper.features.building.listeners

import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.features.building.data.InvisFrameKey
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

class FrameItemInsertListener(
    private val spigotNamespace: SpigotNamespace,
) : Listener {
    @EventHandler
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked

        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return

        val invisibleValue =
            entity.persistentDataContainer.getOrDefault(
                spigotNamespace.get(InvisFrameKey()),
                PersistentDataType.BYTE,
                0,
            )
        val isInvisibleFrame = invisibleValue == 1.toByte()
        if (isInvisibleFrame) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = false
            itemFrame.isGlowing = entity is GlowItemFrame
        }
    }
}
