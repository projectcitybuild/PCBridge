package com.projectcitybuild.features.teleporthistory.usecases

import com.projectcitybuild.CrossServerLocationMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.LastKnownLocation
import com.projectcitybuild.shared.crossteleport.LocationTeleporter
import com.projectcitybuild.repositories.LastKnownLocationRepositoy
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.time.LocalDateTime
import java.util.UUID

class BackUseCaseTest {

    private lateinit var useCase: BackUseCase

    private lateinit var lastKnownLocationRepositoy: LastKnownLocationRepositoy
    private lateinit var locationTeleporter: LocationTeleporter

    private lateinit var player: Player

    @BeforeEach
    fun setUp() {
        lastKnownLocationRepositoy = mock(LastKnownLocationRepositoy::class.java)
        locationTeleporter = mock(LocationTeleporter::class.java)
        player = mock(Player::class.java).also {
            `when`(it.uniqueId).thenReturn(UUID.randomUUID())
        }

        useCase = BackUseCase(
            lastKnownLocationRepositoy,
            locationTeleporter,
        )
    }

    @Test
    fun `should fail if no last known location`() = runTest {
        `when`(lastKnownLocationRepositoy.get(player.uniqueId)).thenReturn(null)

        val result = useCase.teleportBack(player)

        assertEquals(result, Failure(BackUseCase.FailureReason.NO_LAST_LOCATION))
    }

    @Test
    fun `should pass-on teleport failures`() = runTest {
        val destination = CrossServerLocationMock()
        val lastKnownLocation = LastKnownLocation(
            playerUUID = player.uniqueId,
            location = destination,
            createdAt = LocalDateTime.now(),
        )
        `when`(lastKnownLocationRepositoy.get(player.uniqueId)).thenReturn(lastKnownLocation)
        `when`(locationTeleporter.teleport(eq(player), eq(destination), any())).thenReturn(
            Failure(LocationTeleporter.FailureReason.WORLD_NOT_FOUND)
        )

        val result = useCase.teleportBack(player)

        assertEquals(result, Failure(BackUseCase.FailureReason.WORLD_NOT_FOUND))
    }

    @Test
    fun `should teleport to last known location`() = runTest {
        val destination = CrossServerLocationMock()
        val lastKnownLocation = LastKnownLocation(
            playerUUID = player.uniqueId,
            location = destination,
            createdAt = LocalDateTime.now(),
        )
        `when`(lastKnownLocationRepositoy.get(player.uniqueId)).thenReturn(lastKnownLocation)
        `when`(locationTeleporter.teleport(eq(player), eq(destination), any())).thenReturn(
            Success(LocationTeleporter.DestinationType.SAME_SERVER)
        )

        val result = useCase.teleportBack(player)

        assertEquals(result, Success(Unit))
    }
}
