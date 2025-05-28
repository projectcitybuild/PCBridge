package com.projectcitybuild.pcbridge.paper.features.building.listeners

import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.features.building.data.InvisFrameKey
import org.bukkit.entity.Entity
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataType

class InvisFrameListener(
    val spigotNamespace: SpigotNamespace,
) : Listener {
    private val namespacedKey = spigotNamespace.get(InvisFrameKey())

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked

        if (isInvisFrame(entity)) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = false
            itemFrame.isGlowing = entity is GlowItemFrame
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity

        if (isInvisFrame(entity)) {
            val itemFrame = entity as ItemFrame
            itemFrame.isVisible = true
            itemFrame.isGlowing = entity is GlowItemFrame
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHangingPlaceEvent(event: HangingPlaceEvent) {
        val entity = event.entity

        if (isInvisFrame(entity)) {
            event.entity.persistentDataContainer.set(
                namespacedKey,
                PersistentDataType.BYTE,
                1,
            )
        }
    }

    private fun isInvisFrame(entity: Entity): Boolean {
        val isItemFrame = entity is ItemFrame
        if (!isItemFrame) return false

        val invisibleValue = entity.persistentDataContainer.getOrDefault(
            namespacedKey,
            PersistentDataType.BYTE,
            0,
        )
        return invisibleValue == 1.toByte()
    }
}
