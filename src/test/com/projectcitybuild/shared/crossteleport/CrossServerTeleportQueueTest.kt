package com.projectcitybuild.shared.crossteleport

import com.projectcitybuild.CrossServerLocationMock
import com.projectcitybuild.WarpMock
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.TeleportType
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.repositories.QueuedLocationTeleportRepository
import com.projectcitybuild.repositories.QueuedPlayerTeleportRepository
import com.projectcitybuild.shared.locationteleport.CrossServerTeleportQueue
import com.projectcitybuild.stubs.QueuedTeleportMock
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.`when`
import java.util.UUID

class CrossServerTeleportQueueTest {

    private lateinit var teleportQueue: CrossServerTeleportQueue

    private lateinit var config: PlatformConfig
    private lateinit var server: Server
    private lateinit var queuedLocationTeleportRepository: QueuedLocationTeleportRepository
    private lateinit var queuedPlayerTeleportRepository: QueuedPlayerTeleportRepository

    private lateinit var player: Player

    companion object {
        private const val SERVER_NAME = "server"
    }

    @BeforeEach
    fun setUp() {
        config = mock(PlatformConfig::class.java).also {
            `when`(it.get(ConfigKey.SPIGOT_SERVER_NAME)).thenReturn(SERVER_NAME)
        }
        server = mock(Server::class.java)
        queuedLocationTeleportRepository = mock(QueuedLocationTeleportRepository::class.java)
        queuedPlayerTeleportRepository = mock(QueuedPlayerTeleportRepository::class.java)

        player = mock(Player::class.java).also {
            `when`(it.uniqueId).thenReturn(UUID.randomUUID())
            `when`(it.location).thenReturn(mock(Location::class.java))
        }

        teleportQueue = CrossServerTeleportQueue(
            config,
            server,
            queuedLocationTeleportRepository,
            queuedPlayerTeleportRepository,
        )
    }

    @Test
    fun `should return null if no queued location teleport`() = runTest {
        `when`(queuedLocationTeleportRepository.get(player.uniqueId)).thenReturn(null)

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(result, Success(null))
    }

    @Test
    fun `should return null if not the queued location's target server`() = runTest {
        `when`(queuedLocationTeleportRepository.get(player.uniqueId)).thenReturn(
            WarpMock(location = CrossServerLocationMock(serverName = "different_server"))
        )

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(result, Success(null))
    }

    @Test
    fun `should return failure if queued location's world not found`() = runTest {
        val warp = WarpMock(location = CrossServerLocationMock(serverName = SERVER_NAME))

        `when`(queuedLocationTeleportRepository.get(player.uniqueId)).thenReturn(warp)

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(
            result,
            Failure(CrossServerTeleportQueue.FailureReason.WorldNotFound(warp.location.worldName))
        )
    }

    @Test
    fun `should return success if queued location teleport found for this server`() = runTest {
        val warp = WarpMock(location = CrossServerLocationMock(serverName = SERVER_NAME))
        val world = mock(World::class.java)

        `when`(queuedLocationTeleportRepository.get(player.uniqueId)).thenReturn(warp)
        `when`(server.getWorld(warp.location.worldName)).thenReturn(world)

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(
            result,
            Success(
                CrossServerTeleportQueue.Destination.Location(
                    location = Location(
                        world,
                        warp.location.x,
                        warp.location.y,
                        warp.location.z,
                        warp.location.yaw,
                        warp.location.pitch,
                    ),
                    name = warp.name,
                )
            )
        )
    }

    @Test
    fun `should return null if queued player teleport not found`() = runTest {
        `when`(queuedPlayerTeleportRepository.get(player.uniqueId)).thenReturn(null)

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(result, Success(null))
    }

    @Test
    fun `should return null if not the queued destination player's target server`() = runTest {
        val queuedTeleport = QueuedTeleportMock(
            playerUUID = player.uniqueId,
            targetServerName = "different_server",
        )
        `when`(queuedPlayerTeleportRepository.get(player.uniqueId)).thenReturn(queuedTeleport)

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(result, Success(null))
    }

    @Test
    fun `should return failure if destination player not found`() = runTest {
        val targetPlayerUUID = UUID.randomUUID()
        val queuedTeleport = QueuedTeleportMock(
            playerUUID = player.uniqueId,
            targetPlayerUUID = targetPlayerUUID,
            targetServerName = SERVER_NAME,
        )

        `when`(queuedPlayerTeleportRepository.get(player.uniqueId)).thenReturn(queuedTeleport)
        `when`(server.getPlayer(targetPlayerUUID)).thenReturn(null)

        val result = teleportQueue.dequeue(player.uniqueId)

        assertEquals(
            result,
            Failure(CrossServerTeleportQueue.FailureReason.DestinationPlayerNotFound)
        )
    }

    @Test
    fun `should return success if destination player found for this server`() = runTest {
        arrayOf(true, false).forEach { isSilentTeleport ->
            arrayOf(true, false).forEach { isSummon ->
                val targetPlayerUUID = UUID.randomUUID()
                val queuedTeleport = QueuedTeleportMock(
                    playerUUID = player.uniqueId,
                    targetPlayerUUID = targetPlayerUUID,
                    targetServerName = SERVER_NAME,
                    teleportType = if (isSummon) TeleportType.SUMMON else TeleportType.TP,
                    isSilentTeleport = isSilentTeleport,
                )
                val targetPlayer = mock(Player::class.java).also {
                    val location = Location(mock(World::class.java), 0.0, 1.0, 2.0, 3f, 4f)
                    `when`(it.location).thenReturn(location)
                }

                `when`(queuedPlayerTeleportRepository.get(player.uniqueId)).thenReturn(queuedTeleport)
                `when`(server.getPlayer(targetPlayerUUID)).thenReturn(targetPlayer)

                val result = teleportQueue.dequeue(player.uniqueId)

                assertEquals(
                    result,
                    Success(
                        CrossServerTeleportQueue.Destination.Player(
                            destinationPlayer = targetPlayer,
                            location = targetPlayer.location,
                            isSummon = isSummon,
                            isSilentTeleport = isSilentTeleport,
                        )
                    )
                )
            }
        }
    }
}
