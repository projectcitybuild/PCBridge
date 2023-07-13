package com.projectcitybuild.modules.buildtools.nightvision.commands

import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand : SpigotCommand {
    override val label = "nv"
    override val permission = "pcbridge.build.nightvision"
    override val usageHelp = "/nv [on|off]"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.size > 1) {
            throw InvalidCommandArgumentsException()
        }
        if (input.args.isNotEmpty() && !listOf("on", "off").contains(input.args.firstOrNull()?.lowercase())) {
            throw InvalidCommandArgumentsException()
        }

        val duration = Integer.MAX_VALUE
        val amplifier = 1
        val potionEffectType = PotionEffectType.NIGHT_VISION
        val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

        val player = input.player
        val toggleOn = if (input.args.isNotEmpty()) {
            input.args.first().lowercase() == "on"
        } else {
            !player.hasPotionEffect(potionEffectType)
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
