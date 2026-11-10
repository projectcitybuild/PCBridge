package com.projectcitybuild.pcbridge.features.inventories.domain.services

import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.InventoryRepository
import org.bukkit.Bukkit
import org.bukkit.UnsafeValues
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Base64

class InventoryTransitionServiceTest {
    private val service =
        InventoryTransitionService(
            repository = mock<InventoryRepository>(),
            config = MultiverseInventoryConfig(),
        )

    @Test
    fun `decodes Paper schema item maps without a Bukkit serialization marker`() {
        val unsafe = mock<UnsafeValues>()
        val expected = mock<ItemStack>()
        whenever(unsafe.deserializeStack(any())).thenReturn(expected)

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<UnsafeValues> { Bukkit.getUnsafe() }.thenReturn(unsafe)

            val decoded =
                service.decodeItem(
                    JsonParser.parseString(
                        """{"DataVersion":4556,"id":"minecraft:diamond","count":1,"schema_version":1}""",
                    ),
                )

            assertSame(expected, decoded)
        }

        verify(unsafe).deserializeStack(any())
    }

    @Test
    fun `decodes legacy Bukkit item maps`() {
        val expected = mock<ItemStack>()

        mockStatic(ItemStack::class.java).use { itemStacks ->
            itemStacks.`when`<ItemStack> { ItemStack.deserialize(any()) }.thenReturn(expected)

            val decoded =
                service.decodeItem(
                    JsonParser.parseString(
                        """{"==":"org.bukkit.inventory.ItemStack","v":4189,"type":"SEA_LANTERN","amount":2}""",
                    ),
                )

            assertSame(expected, decoded)
            itemStacks.verify { ItemStack.deserialize(any()) }
        }
    }

    @Test
    fun `decodes byte-serialized item data`() {
        val unsafe = mock<UnsafeValues>()
        val expected = mock<ItemStack>()
        whenever(unsafe.deserializeItem(any())).thenReturn(expected)
        val serialized = Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3))

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<UnsafeValues> { Bukkit.getUnsafe() }.thenReturn(unsafe)

            assertSame(expected, service.decodeItem(JsonPrimitive(serialized)))
        }

        verify(unsafe).deserializeItem(any())
    }
}
