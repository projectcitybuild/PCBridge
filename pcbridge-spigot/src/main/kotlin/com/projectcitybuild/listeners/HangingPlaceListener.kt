package com.projectcitybuild.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.support.spigot.SpigotNamespace
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.persistence.PersistentDataType

class HangingPlaceListener(
    private val spigotNamespace: SpigotNamespace,
) : SpigotListener {

    @EventHandler
    fun onHangingPlaceEvent(event: HangingPlaceEvent) {
        /// Unless we use NMS, there's no direct way to set visibility on an ItemStack
        /// when we give the player the item. Therefore, we have to toggle the visibility
        /// and glowing state of the ItemFrame when a player tries to place it in the world
        val entity = event.entity

        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return

        val isInvisibleFrame = event.itemStack?.itemMeta
            ?.persistentDataContainer
            ?.getOrDefault(
                spigotNamespace.invisibleKey,
                PersistentDataType.BYTE,
                0,
            ) ?: false

        val onByte: Byte = 1
        if (isInvisibleFrame == onByte) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = false

            if (entity is GlowItemFrame) {
                itemFrame.isGlowing = true
            }
        }
    }

}
