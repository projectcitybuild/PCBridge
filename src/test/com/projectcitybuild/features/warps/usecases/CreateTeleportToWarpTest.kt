package com.projectcitybuild.features.warps.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.datetime.time.Time
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.time.LocalDateTime

class CreateTeleportToWarpTest {

    private lateinit var useCase: CreateWarp

    private lateinit var warpRepository: WarpRepository
    private lateinit var localEventBroadcaster: LocalEventBroadcaster
    private lateinit var time: Time

    @BeforeEach
    fun setUp() {
        warpRepository = mock(WarpRepository::class.java)
        localEventBroadcaster = mock(LocalEventBroadcaster::class.java)
        time = mock(Time::class.java)

        useCase = CreateWarp(warpRepository, localEventBroadcaster, time)
    }

    @Test
    fun `should fail if warp already exists`() = runTest {
        val warpName = "warp"
        `when`(warpRepository.exists(warpName)).thenReturn(true)

        val result = useCase.createWarp(warpName, SerializableLocation())

        assertEquals(result, Failure(CreateWarp.FailureReason.WARP_ALREADY_EXISTS))
    }

    @Test
    fun `should create new warp`() = runTest {
        val warpName = "warp"
        val location = SerializableLocation()
        val now = LocalDateTime.now()

        `when`(warpRepository.exists(warpName)).thenReturn(false)
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

    @Test
    fun `should emit an event when a warp is created`() = runTest {
        val warpName = "warp"
        val location = SerializableLocation()
        val now = LocalDateTime.now()

        `when`(warpRepository.exists(warpName)).thenReturn(false)
        `when`(time.now()).thenReturn(now)

        val result = useCase.createWarp(warpName, location)

        verify(localEventBroadcaster).emit(any())
        assertEquals(result, Success(Unit))
    }
}
