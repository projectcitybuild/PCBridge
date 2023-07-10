package com.projectcitybuild.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.support.spigot.SpigotNamespace
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Since we don't have direct access to NMS editing, we track an invisible frame with a tag.
 * This tag is saved into the persistentDataContainer so that it persists with the object.
 *
 * - The /invisframe command gives the player a tagged ItemStack of item frames
 * - When the player hangs the frame (HangingPlaceEvent), we apply the tag to the placed entity
 * - When the player places an item in the tagged frame (PlayerInteractEntityEvent), we hide the frame
 * - When the player takes an item from the tagged frame (EntityDamageByEntityEvent), we show the frame
 */
class InvisFrameListener(
    private val spigotNamespace: SpigotNamespace,
) : SpigotListener {

    @EventHandler
    fun onHangingPlace(event: HangingPlaceEvent) {
        val entity = event.entity

        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return

        val invisibleValue = event.itemStack?.itemMeta?.persistentDataContainer?.getOrDefault(
            spigotNamespace.invisibleKey,
            PersistentDataType.BYTE,
            0,
        ) ?: 0.toByte()
        val isInvisibleFrame = invisibleValue == 1.toByte()

        if (isInvisibleFrame) {
            event.entity.persistentDataContainer.set(
                spigotNamespace.invisibleKey,
                PersistentDataType.BYTE,
                1,
            )
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity

        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return

        val isInvisibleFrame = entity.persistentDataContainer.getOrDefault(
            spigotNamespace.invisibleKey,
            PersistentDataType.BYTE,
            0,
        ) == 1.toByte()

        if (isInvisibleFrame) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = true
            itemFrame.isGlowing = entity is GlowItemFrame
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
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
