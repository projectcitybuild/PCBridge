package com.projectcitybuild.pcbridge.features.buildtools.nightvision.commands

import com.projectcitybuild.support.commandapi.ToggleOption
import kotlinx.coroutines.test.runTest
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
        player = Mockito.mock(Player::class.java)

        whenever(player.spigot()).thenReturn(Mockito.mock(Player.Spigot::class.java))
    }

    @Test
    fun `forces on nightvision when specified`() = runTest {
        NightVisionCommand().execute(
            player = player,
            desiredState = ToggleOption.ON,
        )
        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player).addPotionEffect(potionEffect)
    }

    @Test
    fun `forces off nightvision when specified`() = runTest {
        NightVisionCommand().execute(
            player = player,
            desiredState = ToggleOption.OFF,
        )
        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player, never()).addPotionEffect(potionEffect)
    }

    @Test
    fun `toggles on nightvision if no effect`() = runTest {
        whenever(player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
            .thenReturn(false)

        NightVisionCommand().execute(
            player = player,
            desiredState = ToggleOption.UNSPECIFIED,
        )
        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player).addPotionEffect(potionEffect)
    }

    @Test
    fun `toggles off nightvision if already has effect`() = runTest {
        whenever(player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
            .thenReturn(true)

        NightVisionCommand().execute(
            player = player,
            desiredState = ToggleOption.UNSPECIFIED,
        )
        verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
        verify(player, never()).addPotionEffect(potionEffect)
    }
}