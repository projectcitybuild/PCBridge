package com.projectcitybuild.pcbridge.paper.features.buildtools.nightvision.commands

import com.projectcitybuild.pcbridge.paper.features.nightvision.commands.NightVisionCommand
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
    fun `forces on nightvision when specified`() =
        runTest {
            NightVisionCommand().run(
                sender = player,
                args =
                    NightVisionCommand.Args(
                        desiredState = true,
                    ),
            )
            verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
            verify(player).addPotionEffect(potionEffect)
        }

    @Test
    fun `forces off nightvision when specified`() =
        runTest {
            NightVisionCommand().run(
                sender = player,
                args =
                    NightVisionCommand.Args(
                        desiredState = false,
                    ),
            )
            verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
            verify(player, never()).addPotionEffect(potionEffect)
        }

    @Test
    fun `toggles on nightvision if no effect`() =
        runTest {
            whenever(player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                .thenReturn(false)

            NightVisionCommand().run(
                sender = player,
                args =
                    NightVisionCommand.Args(
                        desiredState = null,
                    ),
            )
            verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
            verify(player).addPotionEffect(potionEffect)
        }

    @Test
    fun `toggles off nightvision if already has effect`() =
        runTest {
            whenever(player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
                .thenReturn(true)

            NightVisionCommand().run(
                sender = player,
                args =
                    NightVisionCommand.Args(
                        desiredState = null,
                    ),
            )
            verify(player).removePotionEffect(PotionEffectType.NIGHT_VISION)
            verify(player, never()).addPotionEffect(potionEffect)
        }
}
