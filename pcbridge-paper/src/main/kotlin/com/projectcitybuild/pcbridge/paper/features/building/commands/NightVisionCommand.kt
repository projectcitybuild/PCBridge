package com.projectcitybuild.pcbridge.paper.features.building.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnOffArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override val description: String = "Toggles night-vision mode"

    override fun buildLiteral(): CommandNode {
        return Commands.literal("nv")
            .requiresPermission(PermissionNode.BUILDING_NIGHT_VISION)
            .then(
                Commands.argument("enabled", OnOffArgument())
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()

        val duration = Integer.MAX_VALUE
        val amplifier = 1
        val potionEffectType = PotionEffectType.NIGHT_VISION
        val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

        val desiredState = runCatching {
            context.getArgument("enabled", Boolean::class.java)
        }.getOrNull()

        val toggleOn =
            if (desiredState == null) {
                !player.hasPotionEffect(potionEffectType)
            } else {
                desiredState == true
            }

        player.removePotionEffect(potionEffectType)

        if (toggleOn) {
            player.addPotionEffect(potionEffect)
            player.sendRichMessage(l10n.nightVisionToggledOn)
        } else {
            player.sendRichMessage(l10n.nightVisionToggledOff)
        }
    }
}
