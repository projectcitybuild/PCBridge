package com.projectcitybuild.features.chat.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.repositories.PlayerConfigRepository
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`

class MuteUseCaseTest {

    private lateinit var useCase: MuteUseCase

    private lateinit var playerConfigRepository: PlayerConfigRepository
    private lateinit var nameGuesser: NameGuesser

    @BeforeEach
    fun setUp() {
        playerConfigRepository = mock(PlayerConfigRepository::class.java)
        nameGuesser = mock(NameGuesser::class.java)

        useCase = MuteUseCase(
            playerConfigRepository,
            nameGuesser,
        )
    }

    @Test
    fun `returns failure if player not online`() = runTest {
        `when`(nameGuesser.guessClosest(any(), any<Collection<Player>>(), any()))
            .thenReturn(null)

        val result = useCase.execute(
            willBeMuted = true,
            targetPlayerName = "name",
            onlinePlayers = emptyList(),
        )

        assertEquals(result, Failure(MuteUseCase.FailureReason.PLAYER_NOT_ONLINE))
    }

    @Test
    fun `returns success if player online`() = runTest {
        val targetPlayer = mock(Player::class.java)

        `when`(nameGuesser.guessClosest(any(), any<Collection<Player>>(), any()))
            .thenReturn(targetPlayer)

        val result = useCase.execute(
            willBeMuted = true,
            targetPlayerName = "name",
            onlinePlayers = listOf(targetPlayer),
        )

        assertEquals(result, Success(targetPlayer))
    }
}
