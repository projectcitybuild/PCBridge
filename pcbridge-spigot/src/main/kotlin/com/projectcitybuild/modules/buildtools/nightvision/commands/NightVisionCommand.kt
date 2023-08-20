package com.projectcitybuild.modules.buildtools.nightvision.commands

import com.projectcitybuild.support.commandapi.ToggleOption
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand {
    fun execute(
        player: Player,
        desiredState: ToggleOption,
    ) {
        val duration = Integer.MAX_VALUE
        val amplifier = 1
        val potionEffectType = PotionEffectType.NIGHT_VISION
        val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

        val toggleOn = if (desiredState == ToggleOption.UNSPECIFIED) {
            !player.hasPotionEffect(potionEffectType)
        } else {
            desiredState == ToggleOption.ON
        }

        player.removePotionEffect(potionEffectType)

        if (toggleOn) {
            player.addPotionEffect(potionEffect)
            player.send().info("NightVision toggled on. Type /nv to turn it off")
        } else {
            player.send().info("NightVision toggled off")
        }
    }
}
