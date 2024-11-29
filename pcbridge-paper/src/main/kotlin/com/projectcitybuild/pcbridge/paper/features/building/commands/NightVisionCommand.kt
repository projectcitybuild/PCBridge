package com.projectcitybuild.pcbridge.paper.features.building.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnOffArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("nv")
            .requiresPermission(PermissionNode.BUILDING_NIGHT_VISION)
            .then(
                Commands.argument("enabled", OnOffArgument())
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val duration = Integer.MAX_VALUE
        val amplifier = 1
        val potionEffectType = PotionEffectType.NIGHT_VISION
        val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

        val desiredState = runCatching {
            context.getArgument("enabled", Boolean::class.java)
        }.getOrNull()

        val toggleOn =
            if (desiredState == null) {
                !sender.hasPotionEffect(potionEffectType)
            } else {
                desiredState == true
            }

        sender.removePotionEffect(potionEffectType)

        if (toggleOn) {
            sender.addPotionEffect(potionEffect)
            sender.sendMessage(
                Component.text("NightVision toggled on. Type /nv to turn it off")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC),
            )
        } else {
            sender.sendMessage(
                Component.text("NightVision toggled off")
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC),
            )
        }
    }
}
