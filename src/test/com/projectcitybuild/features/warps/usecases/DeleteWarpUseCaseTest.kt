package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.repositories.WarpRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`

class DeleteWarpUseCaseTest {

    private lateinit var useCase: DeleteWarpUseCase

    private lateinit var warpRepository: WarpRepository
    private lateinit var localEventBroadcaster: LocalEventBroadcaster

    @BeforeEach
    fun setUp() {
        warpRepository = mock(WarpRepository::class.java)
        localEventBroadcaster = mock(LocalEventBroadcaster::class.java)

        useCase = DeleteWarpUseCase(warpRepository, localEventBroadcaster)
    }

    @Test
    fun `should fail if warp doesn't exists`() = runTest {
        val warpName = "warp"
        `when`(warpRepository.exists(warpName)).thenReturn(false)

        val result = useCase.deleteWarp(warpName)

        assertEquals(result, Failure(DeleteWarpUseCase.FailureReason.WARP_NOT_FOUND))
    }

    @Test
    fun `should delete existing warp`() = runTest {
        val warpName = "warp"
        `when`(warpRepository.exists(warpName)).thenReturn(true)

        val result = useCase.deleteWarp(warpName)

        verify(warpRepository, times(1)).delete(warpName)
        assertEquals(result, Success(Unit))
    }

    @Test
    fun `should emit an event when a warp is deleted`() = runTest {
        val warpName = "warp"
        `when`(warpRepository.exists(warpName)).thenReturn(true)

        val result = useCase.deleteWarp(warpName)

        verify(localEventBroadcaster).emit(any())
        assertEquals(result, Success(Unit))
    }
}
