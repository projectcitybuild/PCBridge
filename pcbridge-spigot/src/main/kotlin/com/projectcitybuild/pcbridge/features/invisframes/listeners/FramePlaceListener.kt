package com.projectcitybuild.pcbridge.features.invisframes.listeners

import com.projectcitybuild.pcbridge.support.spigot.SpigotNamespace
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.persistence.PersistentDataType

class FramePlaceListener(
    private val spigotNamespace: SpigotNamespace,
) : Listener {
    @EventHandler
    fun onHangingPlaceEvent(event: HangingPlaceEvent) {
        val isItemFrame = event.entity is ItemFrame
        if (!isItemFrame) return

        val invisibleValue =
            event.itemStack?.itemMeta?.persistentDataContainer?.getOrDefault(
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
}
