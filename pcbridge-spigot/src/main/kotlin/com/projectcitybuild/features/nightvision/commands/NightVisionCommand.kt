package com.projectcitybuild.features.nightvision.commands

import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.BadCommandUsageException
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import com.projectcitybuild.support.textcomponent.send
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand: SpigotCommand<NightVisionCommand.Args> {
    override val label = "nv"

    override val usage = CommandHelpBuilder()

    override suspend fun run(sender: CommandSender, args: Args) {
        val player = sender as? Player
        checkNotNull (player) {
            "Only players can use this command"
        }
        val duration = Integer.MAX_VALUE
        val amplifier = 1
        val potionEffectType = PotionEffectType.NIGHT_VISION
        val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

        val toggleOn = if (args.desiredState == null) {
            !player.hasPotionEffect(potionEffectType)
        } else {
            args.desiredState == true
        }

        player.removePotionEffect(potionEffectType)

        if (toggleOn) {
            player.addPotionEffect(potionEffect)
            player.sendMessage(
                Component.text("NightVision toggled on. Type /nv to turn it off")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC)
            )
        } else {
            player.sendMessage(
                Component.text("NightVision toggled off")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC)
            )
        }
    }

    data class Args(
        val desiredState: Boolean? = null,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    return Args()
                }
                return when (args[0].lowercase()) {
                    "on" -> Args(desiredState = true)
                    "off" -> Args(desiredState = false)
                    else -> throw BadCommandUsageException()
                }
            }
        }
    }
}
