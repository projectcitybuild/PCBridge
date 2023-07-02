package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.config.adapters.MemoryKeyValueStorage
import com.projectcitybuild.repositories.WarpRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class GetWarpListTest {

    private lateinit var useCase: GetWarpList
    private lateinit var keyValueStorage: MemoryKeyValueStorage
    private lateinit var config: Config

    private val warpRepository = mock(WarpRepository::class.java)

    @BeforeEach
    fun setUp() {
        keyValueStorage = MemoryKeyValueStorage()
        config = Config(keyValueStorage)
        useCase = GetWarpList(
            warpRepository,
            config,
        )
    }

    @Test
    fun `should return all warps sorted`() = runTest {
        val warps = listOf("b", "c", "a",)
        whenever(warpRepository.names()).thenReturn(warps)
        keyValueStorage.set(ConfigKeys.warpsPerPage, warps.size)

        val received = useCase.getList(page = 1)
        val expected = listOf("a", "b", "c")

        assertEquals(expected, received?.warps)
    }

    @Test
    fun `should return all warps when less than page max`() = runTest {
        val warps = MutableList(3) { index -> "warp_$index" }

        whenever(warpRepository.names()).thenReturn(warps)
        keyValueStorage.set(ConfigKeys.warpsPerPage, 3)

        val received = useCase.getList(page = 1)

        warps.forEach {
            assertTrue(received!!.warps.contains(it))
        }
        assertEquals(warps.size, received?.warps?.size)
        assertEquals(1, received?.currentPage)
        assertEquals(1, received?.totalPages)
    }

    @Test
    fun `should return warps paginated when more than page max`() = runTest {
        val warps = MutableList(5) { index -> "warp_$index" }

        whenever(warpRepository.names()).thenReturn(warps)
        keyValueStorage.set(ConfigKeys.warpsPerPage, 2)

        val firstPage = useCase.getList(page = 1)
        assertEquals(listOf("warp_0", "warp_1"), firstPage?.warps)
        assertEquals(1, firstPage?.currentPage)
        assertEquals(3, firstPage?.totalPages)

        val secondPage = useCase.getList(page = 2)
        assertEquals(listOf("warp_2", "warp_3"), secondPage?.warps)
        assertEquals(2, secondPage?.currentPage)
        assertEquals(3, secondPage?.totalPages)

        val thirdPage = useCase.getList(page = 3)
        assertEquals(listOf("warp_4"), thirdPage?.warps)
        assertEquals(3, thirdPage?.currentPage)
        assertEquals(3, thirdPage?.totalPages)

        val fourthPage = useCase.getList(page = 4)
        assertEquals(listOf("warp_4"), fourthPage?.warps)
        assertEquals(3, fourthPage?.currentPage)
        assertEquals(3, fourthPage?.totalPages)
    }
}
