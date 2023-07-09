package com.projectcitybuild.plugin.commands

import com.projectcitybuild.commands.NightVisionCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NightVisionCommandTest {
    private lateinit var player: Player

    private val potionEffect: PotionEffect
        get() {
            val duration = Integer.MAX_VALUE
            val amplifier = 1

            return PotionEffect(
                PotionEffectType.NIGHT_VISION,
                duration,
                amplifier,
            )
        }

    @BeforeEach
    fun setUp() {
        player = mock(Player::class.java)

        whenever(player.spigot()).thenReturn(mock(Player.Spigot::class.java))
    }

    @Test
    fun `forces on nightvision when specified`() = runTest {
        val input = SpigotCommandInput(
            sender = player,
            args = listOf("on"),
            isConsole = false,
        )
        NightVisionCommand().execute(input)

        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player).addPotionEffect(potionEffect)
    }

    @Test
    fun `forces off nightvision when specified`() = runTest {
        val input = SpigotCommandInput(
            sender = player,
            args = listOf("off"),
            isConsole = false,
        )
        NightVisionCommand().execute(input)

        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player, never()).addPotionEffect(potionEffect)
    }

    @Test
    fun `toggles on nightvision if no effect`() = runTest {
        whenever(player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
            .thenReturn(false)

        val input = SpigotCommandInput(
            sender = player,
            args = emptyList(),
            isConsole = false,
        )
        NightVisionCommand().execute(input)

        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player).addPotionEffect(potionEffect)
    }

    @Test
    fun `toggles off nightvision if alerady has effect`() = runTest {
        whenever(player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
            .thenReturn(true)

        val input = SpigotCommandInput(
            sender = player,
            args = emptyList(),
            isConsole = false,
        )
        NightVisionCommand().execute(input)

        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player, never()).addPotionEffect(potionEffect)
    }
}
