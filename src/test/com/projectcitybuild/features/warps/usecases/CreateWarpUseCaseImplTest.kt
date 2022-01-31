package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.CrossServerLocationMock
import com.projectcitybuild.WarpMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.datetime.Time
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mock
import java.time.LocalDateTime

class CreateWarpUseCaseImplTest {

    private lateinit var useCase: CreateWarpUseCase

    private lateinit var warpRepository: WarpRepository
    private lateinit var time: Time

    @BeforeEach
    fun setUp() {
        warpRepository = mock(WarpRepository::class.java)
        time = mock(Time::class.java)

        useCase = CreateWarpUseCaseImpl(warpRepository, time)
    }

    @Test
    fun `should fail if warp already exists`() = runTest {
        val warpName = "warp"
        `when`(warpRepository.exists(warpName)).thenReturn(true)

        val result = useCase.createWarp(warpName, CrossServerLocationMock())

        assertEquals(result, Failure(CreateWarpUseCase.FailureReason.WARP_ALREADY_EXISTS))
    }

    @Test
    fun `should create new warp`() = runTest {
        val warpName = "warp"
        val location = CrossServerLocationMock()
        val now = LocalDateTime.now()

        `when`(warpRepository.exists(warpName)).thenReturn(true)
        `when`(time.now()).thenReturn(now)

        val result = useCase.createWarp(warpName, location)

        val expectedWarp = Warp(
            warpName,
            location,
            now,
        )

        verify(warpRepository, times(1)).add(expectedWarp)
        assertEquals(result, Success(Unit))
    }
}