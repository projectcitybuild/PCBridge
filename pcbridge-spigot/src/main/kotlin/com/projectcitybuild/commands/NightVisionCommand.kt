package com.projectcitybuild.commands

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
    override val usageHelp = "/nv [off|on]"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.size > 1 || !listOf("on", "off").contains(input.args.first())) {
            throw InvalidCommandArgumentsException()
        }
        val duration = -1
        val amplifier = 0
        val potionEffectType = PotionEffectType.NIGHT_VISION
        val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

        val player = input.player
        val alreadyHasEffect = player.hasPotionEffect(potionEffectType)

        player.removePotionEffect(potionEffectType)

        if (input.args.first() == "on" || !alreadyHasEffect) {
            player.addPotionEffect(potionEffect)
            player.send().info("Nightvision toggled on. Type /nv to turn it off")
        } else {
            player.send().info("Nightvision toggled off")
        }
    }
}
