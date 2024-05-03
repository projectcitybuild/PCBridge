package com.projectcitybuild.features.invisframes.listeners

import com.projectcitybuild.support.spigot.SpigotNamespace
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType

class FrameItemRemoveListener(
    private val spigotNamespace: SpigotNamespace,
) : Listener {
    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity

        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return

        val invisibleValue = entity.persistentDataContainer.getOrDefault(
            spigotNamespace.invisibleKey,
            PersistentDataType.BYTE,
            0,
        )
        val isInvisibleFrame = invisibleValue == 1.toByte()
        if (isInvisibleFrame) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = true
            itemFrame.isGlowing = entity is GlowItemFrame
        }
    }
}
