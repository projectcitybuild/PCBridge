package com.projectcitybuild.pcbridge.runtime.teleportation

import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
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
}
