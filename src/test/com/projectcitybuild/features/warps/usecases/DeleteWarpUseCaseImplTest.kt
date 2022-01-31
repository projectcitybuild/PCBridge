package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.usecases.deletewarp.DeleteWarpUseCase
import com.projectcitybuild.features.warps.usecases.deletewarp.DeleteWarpUseCaseImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock

class DeleteWarpUseCaseImplTest {

    private lateinit var useCase: DeleteWarpUseCase

    private lateinit var warpRepository: WarpRepository

    @BeforeEach
    fun setUp() {
        warpRepository = mock(WarpRepository::class.java)

        useCase = DeleteWarpUseCaseImpl(warpRepository)
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
}