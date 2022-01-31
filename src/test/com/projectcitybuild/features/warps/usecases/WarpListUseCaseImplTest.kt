package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.usecases.warplist.WarpListUseCase
import com.projectcitybuild.features.warps.usecases.warplist.WarpListUseCaseImpl
import com.projectcitybuild.modules.config.PlatformConfig
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock

class WarpListUseCaseImplTest {

    private lateinit var useCase: WarpListUseCase

    private val warpRepository = mock(WarpRepository::class.java)
    private val config = mock(PlatformConfig::class.java)

    @BeforeEach
    fun setUp() {
        useCase = WarpListUseCaseImpl(
            warpRepository,
            config,
        )
    }

    @Test
    fun `should return all warps sorted`() = runTest {
        val warps = listOf("b", "c", "a",)
        `when`(warpRepository.names()).thenReturn(warps)
        `when`(config.get(PluginConfig.WARPS_PER_PAGE)).thenReturn(warps.size)

        val received = useCase.getList(page = 1)
        val expected = listOf("a", "b", "c")

        assertEquals(expected, received?.warps)
    }

    @Test
    fun `should return all warps when less than page max`() = runTest {
        val warps = MutableList(3) { index -> "warp_$index" }

        `when`(warpRepository.names()).thenReturn(warps)
        `when`(config.get(PluginConfig.WARPS_PER_PAGE)).thenReturn(3)

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

        `when`(warpRepository.names()).thenReturn(warps)
        `when`(config.get(PluginConfig.WARPS_PER_PAGE)).thenReturn(2)

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