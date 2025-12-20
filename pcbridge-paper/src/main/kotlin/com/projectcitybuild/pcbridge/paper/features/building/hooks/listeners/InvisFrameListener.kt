package com.projectcitybuild.pcbridge.paper.features.building.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotNamespace
import com.projectcitybuild.pcbridge.paper.features.building.buildingTracer
import com.projectcitybuild.pcbridge.paper.features.building.domain.data.InvisFrameKey
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import io.papermc.paper.persistence.PersistentDataViewHolder
import org.bukkit.block.Container
import org.bukkit.entity.GlowItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.persistence.PersistentDataType


class InvisFrameListener(
    val spigotNamespace: SpigotNamespace,
) : Listener {
    private val namespacedKey = spigotNamespace.get(InvisFrameKey)

    @EventHandler(ignoreCancelled = true)
    fun onPlayerItemFrameChangeEvent(
        event: PlayerItemFrameChangeEvent,
    ) = event.scopedSync(buildingTracer, this::class.java) {
        val frame = event.itemFrame
        if (!isInvisFrame(frame)) return@scopedSync

        when (event.action) {
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE -> {
                frame.isVisible = false
                frame.isGlowing = frame is GlowItemFrame
            }
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.REMOVE -> {
                frame.isVisible = true
                frame.isGlowing = false
            }
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE -> {
                val player = event.player

                // Allow rotation
                if (player.isSneaking) return@scopedSync

                // Allow interacting with containers behind frames
                val attachedFace = frame.attachedFace
                val mount = frame.location.block.getRelative(attachedFace)
                val state = mount.state
                if (state is Container) {
                    event.isCancelled = true
                    player.openInventory(state.inventory)
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHangingPlaceEvent(
        event: HangingPlaceEvent
    ) = event.scopedSync(buildingTracer, this::class.java) {
        val itemStack = event.itemStack
            ?: return@scopedSync

        if (isInvisFrame(itemStack)) {
            event.entity.persistentDataContainer.set(
                namespacedKey,
                PersistentDataType.BOOLEAN,
                true,
            )
        }
    }

    private fun isInvisFrame(holder: PersistentDataViewHolder): Boolean {
        return holder.persistentDataContainer.getOrDefault(
            namespacedKey,
            PersistentDataType.BOOLEAN,
            false,
        )
    }
}
