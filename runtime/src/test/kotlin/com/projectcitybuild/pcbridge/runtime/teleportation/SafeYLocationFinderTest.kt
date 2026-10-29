package com.projectcitybuild.pcbridge.runtime.teleportation

import org.bukkit.World
import org.bukkit.block.Block
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class SafeYLocationFinderTest {
    @Test
    fun `does not load an unloaded chunk while searching for a safe y`() {
        val world = mock(World::class.java)
        whenever(world.isChunkLoaded(1, -1)).thenReturn(false)

        val result = SafeYLocationFinder().findY(world, 16, -1)

        assertNull(result)
        verify(world, never()).getBlockAt(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun `checks only the floor until reaching safe flat terrain`() {
        val world = mock(World::class.java)
        val blocks = mutableMapOf<Int, Block>()
        var blockLookups = 0
        whenever(world.minHeight).thenReturn(0)
        whenever(world.maxHeight).thenReturn(16)
        whenever(world.environment).thenReturn(World.Environment.NORMAL)
        whenever(world.isChunkLoaded(0, 0)).thenReturn(true)
        whenever(world.getBlockAt(any(), any(), any())).thenAnswer { invocation ->
            blockLookups++
            val y = invocation.getArgument<Int>(1)
            blocks.getOrPut(y) {
                mock(Block::class.java).also { block ->
                    whenever(block.y).thenReturn(y)
                }
            }
        }

        val result = SafeYLocationFinder().findY(
            world,
            0,
            0,
            isSafeFloor = { it.y == 4 },
            isAir = { true },
        )

        assertEquals(5, result)
        assertEquals(12, blockLookups)
    }

    @Test
    fun `searches uneven terrain from highest to lowest`() {
        val world = mockWorld()
        var blockLookups = 0
        whenever(world.getBlockAt(any(), any(), any())).thenAnswer { invocation ->
            blockLookups++
            mockBlock(invocation.getArgument<Int>(1))
        }

        val result = SafeYLocationFinder().findY(
            world,
            0,
            0,
            isSafeFloor = { it.y == 3 || it.y == 7 },
            isAir = { true },
        )

        assertEquals(8, result)
        assertEquals(9, blockLookups)
    }

    @Test
    fun `checks each floor once when no safe landing exists`() {
        val world = mockWorld()
        var blockLookups = 0
        whenever(world.getBlockAt(any(), any(), any())).thenAnswer {
            blockLookups++
            mockBlock(0)
        }

        val result = SafeYLocationFinder().findY(
            world,
            0,
            0,
            isSafeFloor = { false },
            isAir = { true },
        )

        assertNull(result)
        assertEquals(14, blockLookups)
    }

    private fun mockWorld(): World = mock(World::class.java).also { world ->
        whenever(world.minHeight).thenReturn(0)
        whenever(world.maxHeight).thenReturn(16)
        whenever(world.environment).thenReturn(World.Environment.NORMAL)
        whenever(world.isChunkLoaded(0, 0)).thenReturn(true)
    }

    private fun mockBlock(y: Int): Block = mock(Block::class.java).also { block ->
        whenever(block.y).thenReturn(y)
    }
}
