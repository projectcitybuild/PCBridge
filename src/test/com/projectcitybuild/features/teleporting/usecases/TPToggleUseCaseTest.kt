package com.projectcitybuild.features.teleporting.usecases

import com.projectcitybuild.PlayerConfigMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.repositories.PlayerConfigRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class TPToggleUseCaseTest {

    private lateinit var useCase: TPToggleUseCase

    private lateinit var playerConfigRepository: PlayerConfigRepository

    @BeforeEach
    fun setUp() {
        playerConfigRepository = mock(PlayerConfigRepository::class.java)

        useCase = TPToggleUseCase(playerConfigRepository)
    }

    @Test
    fun `should toggle to specified value`() = runTest {
        arrayOf(true, false).forEach { newDesiredState ->
            val uuid = UUID.randomUUID()
            val config = PlayerConfigMock(uuid).apply { isAllowingTPs = !newDesiredState }

            `when`(playerConfigRepository.get(uuid)).thenReturn(config)

            val result = useCase.toggle(uuid, newDesiredState)

            assertEquals(result, Success(newDesiredState))
        }
    }

    @Test
    fun `should reverse saved value when no specified value`() = runTest {
        arrayOf(true, false).forEach { currentState ->
            val uuid = UUID.randomUUID()
            val config = PlayerConfigMock(uuid).apply { isAllowingTPs = currentState }

            `when`(playerConfigRepository.get(uuid)).thenReturn(config)

            val result = useCase.toggle(uuid, null)

            assertEquals(result, Success(!currentState))
        }
    }

    @Test
    fun `should fail when toggling on while already on`() = runTest {
        val uuid = UUID.randomUUID()
        val config = PlayerConfigMock(uuid).apply { isAllowingTPs = true }

        `when`(playerConfigRepository.get(uuid)).thenReturn(config)

        val result = useCase.toggle(uuid, true)

        assertEquals(result, Failure(TPToggleUseCase.FailureReason.ALREADY_TOGGLED_ON))
    }

    @Test
    fun `should fail when toggling off while already off`() = runTest {
        val uuid = UUID.randomUUID()
        val config = PlayerConfigMock(uuid).apply { isAllowingTPs = false }

        `when`(playerConfigRepository.get(uuid)).thenReturn(config)

        val result = useCase.toggle(uuid, false)

        assertEquals(result, Failure(TPToggleUseCase.FailureReason.ALREADY_TOGGLED_OFF))
    }
}
