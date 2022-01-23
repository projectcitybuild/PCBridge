package com.projectcitybuild.test

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.modules.proxyadapter.broadcast.MessageBroadcaster
import com.projectcitybuild.modules.proxyadapter.kick.PlayerKicker
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.usecases.BanUseCase
import com.projectcitybuild.features.bans.usecases.BanUseCaseImpl
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class BanUseCaseImplTest {

    private lateinit var useCase: BanUseCaseImpl

    private val banRepository = mock(BanRepository::class.java)
    private val playerUUIDRepository = mock(PlayerUUIDRepository::class.java)
    private val playerKicker = mock(PlayerKicker::class.java)
    private val messageBroadcaster = mock(MessageBroadcaster::class.java)

    @BeforeEach
    fun setUp() {
        useCase = BanUseCaseImpl(
            banRepository,
            playerUUIDRepository,
            playerKicker,
            messageBroadcaster,
        )
    }

    @Test
    fun `ban should fail when player doesn't exist`() = runTest {
        val playerName = "name"
        `when`(playerUUIDRepository.request(playerName)).thenReturn(null)

        val result = useCase.ban(playerName, null, "name", null)
        assertEquals(result, Failure(BanUseCase.FailureReason.PlayerDoesNotExist))
    }
}