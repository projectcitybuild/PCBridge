package com.projectcitybuild.pcbridge.features.building.hooks.listeners

import com.projectcitybuild.pcbridge.features.building.domain.data.InvisFrameKey
import com.projectcitybuild.pcbridge.platform.paper.namespace.SpigotNamespace
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import org.bukkit.entity.Hanging
import org.bukkit.entity.ItemFrame
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class InvisFrameListenerTest {
    private val plugin = mock(JavaPlugin::class.java).also { whenever(it.namespace()).thenReturn("pcbridge") }
    private val namespace = SpigotNamespace(plugin)
    private val key = namespace.get(InvisFrameKey)
    private val listener = InvisFrameListener(namespace)

    @Test
    fun `ignores a frame that is not marked as an invisible frame`() {
        val frame = mock(ItemFrame::class.java)
        val data = mock(PersistentDataContainer::class.java)
        whenever(frame.persistentDataContainer).thenReturn(data)
        whenever(data.getOrDefault(key, PersistentDataType.BOOLEAN, false)).thenReturn(false)
        val event = mock(PlayerItemFrameChangeEvent::class.java)
        whenever(event.itemFrame).thenReturn(frame)

        listener.onPlayerItemFrameChangeEvent(event)

        verify(frame, never()).isVisible = false
    }

    @Test
    fun `hides marked frame when placed`() {
        val frame = mock(ItemFrame::class.java)
        val data = mock(PersistentDataContainer::class.java)
        whenever(frame.persistentDataContainer).thenReturn(data)
        whenever(data.getOrDefault(key, PersistentDataType.BOOLEAN, false)).thenReturn(true)
        val event = mock(PlayerItemFrameChangeEvent::class.java)
        whenever(event.itemFrame).thenReturn(frame)
        whenever(event.action).thenReturn(PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE)

        listener.onPlayerItemFrameChangeEvent(event)

        verify(frame).isVisible = false
        verify(frame).isGlowing = false
    }

    @Test
    fun `marks a placed invisible frame item on the new entity`() {
        val item = mock(org.bukkit.inventory.ItemStack::class.java)
        val itemData = mock(PersistentDataContainer::class.java)
        whenever(item.persistentDataContainer).thenReturn(itemData)
        whenever(itemData.getOrDefault(key, PersistentDataType.BOOLEAN, false)).thenReturn(true)
        val entity = mock(Hanging::class.java)
        val entityData = mock(PersistentDataContainer::class.java)
        whenever(entity.persistentDataContainer).thenReturn(entityData)
        val event = mock(HangingPlaceEvent::class.java)
        whenever(event.itemStack).thenReturn(item)
        whenever(event.entity).thenReturn(entity)

        listener.onHangingPlaceEvent(event)

        verify(entityData).set(key, PersistentDataType.BOOLEAN, true)
    }

    @Test
    fun `does not mark an unrelated hanging entity`() {
        val item = mock(org.bukkit.inventory.ItemStack::class.java)
        val itemData = mock(PersistentDataContainer::class.java)
        whenever(item.persistentDataContainer).thenReturn(itemData)
        whenever(itemData.getOrDefault(key, PersistentDataType.BOOLEAN, false)).thenReturn(false)
        val entityData = mock(PersistentDataContainer::class.java)
        val entity = mock(Hanging::class.java)
        whenever(entity.persistentDataContainer).thenReturn(entityData)
        val event = mock(HangingPlaceEvent::class.java)
        whenever(event.itemStack).thenReturn(item)
        whenever(event.entity).thenReturn(entity)

        listener.onHangingPlaceEvent(event)

        verify(entityData, never()).set(key, PersistentDataType.BOOLEAN, true)
    }
}
