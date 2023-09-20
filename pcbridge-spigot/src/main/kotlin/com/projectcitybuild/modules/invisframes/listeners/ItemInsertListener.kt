package com.projectcitybuild.modules.invisframes.listeners

import com.projectcitybuild.support.spigot.SpigotNamespace
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

class ItemInsertListener(
    private val spigotNamespace: SpigotNamespace,
) {
    fun handle(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked

        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return

        val isInvisibleFrame = entity.persistentDataContainer.getOrDefault(
            spigotNamespace.invisibleKey,
            PersistentDataType.BYTE,
            0,
        ) == 1.toByte()

        if (isInvisibleFrame) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = false
            itemFrame.isGlowing = entity is GlowItemFrame
        }
    }
}
