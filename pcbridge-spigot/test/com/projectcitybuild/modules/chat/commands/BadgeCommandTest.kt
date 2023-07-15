package com.projectcitybuild.modules.chat.commands

import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.support.commandapi.ToggleOption
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class BadgeCommandTest {
    private lateinit var playerConfigRepository: PlayerConfigRepository
    private lateinit var player: Player
    private lateinit var command: BadgeCommand

    private fun makePlayerConfig(isBadgeOff: Boolean): PlayerConfig {
        return PlayerConfig(
            id = 123,
            uuid = UUID.randomUUID(),
            isMuted = false,
            isChatBadgeDisabled = isBadgeOff,
            firstSeen = LocalDateTime.now(),
        )
    }

    @BeforeEach
    fun setUp() {
        playerConfigRepository = mock(PlayerConfigRepository::class.java)
        player = mock(Player::class.java)
        command = BadgeCommand(playerConfigRepository)

        whenever(player.spigot()).thenReturn(mock(Player.Spigot::class.java))
    }

    @Test
    fun `forces badge off if specified`() {
        val initialConfig = makePlayerConfig(isBadgeOff = false)
        val uuid = UUID.randomUUID()
        whenever(player.uniqueId).thenReturn(uuid)
        whenever(playerConfigRepository.get(uuid)).thenReturn(initialConfig)

        command.execute(
            commandSender = player,
            desiredState = ToggleOption.OFF,
        )
        argumentCaptor<PlayerConfig>().apply {
            verify(playerConfigRepository).save(capture())
            assertEquals(initialConfig.copy(isChatBadgeDisabled = true), firstValue)
        }
    }

    @Test
    fun `forces badge on if specified`() {
        val initialConfig = makePlayerConfig(isBadgeOff = true)
        val uuid = UUID.randomUUID()
        whenever(player.uniqueId).thenReturn(uuid)
        whenever(playerConfigRepository.get(uuid)).thenReturn(initialConfig)

        command.execute(
            commandSender = player,
            desiredState = ToggleOption.ON,
        )
        argumentCaptor<PlayerConfig>().apply {
            verify(playerConfigRepository).save(capture())
            assertEquals(initialConfig.copy(isChatBadgeDisabled = false), firstValue)
        }
    }

    @Test
    fun `toggles badge off if unspecified`() {
        val initialConfig = makePlayerConfig(isBadgeOff = false)
        val uuid = UUID.randomUUID()
        whenever(player.uniqueId).thenReturn(uuid)
        whenever(playerConfigRepository.get(uuid)).thenReturn(initialConfig)

        command.execute(
            commandSender = player,
            desiredState = ToggleOption.UNSPECIFIED,
        )
        argumentCaptor<PlayerConfig>().apply {
            verify(playerConfigRepository).save(capture())
            assertEquals(initialConfig.copy(isChatBadgeDisabled = true), firstValue)
        }
    }

    @Test
    fun `toggles badge on if unspecified`() {
        val initialConfig = makePlayerConfig(isBadgeOff = true)
        val uuid = UUID.randomUUID()
        whenever(player.uniqueId).thenReturn(uuid)
        whenever(playerConfigRepository.get(uuid)).thenReturn(initialConfig)

        command.execute(
            commandSender = player,
            desiredState = ToggleOption.UNSPECIFIED,
        )
        argumentCaptor<PlayerConfig>().apply {
            verify(playerConfigRepository).save(capture())
            assertEquals(initialConfig.copy(isChatBadgeDisabled = false), firstValue)
        }
    }
}