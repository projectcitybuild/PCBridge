package com.projectcitybuild.pcbridge.paper.features.building.hooks.commands

import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.building.buildingTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.arguments.OnOffArgument
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override val description: String = "Toggles night-vision mode"

    override fun literal(): PaperCommandNode {
        return Commands.literal("nv")
            .requiresPermission(PermissionNode.BUILDING_NIGHT_VISION)
            .then(
                Commands.argument("enabled", OnOffArgument())
                    .executesSuspending(plugin, ::execute),
            )
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(buildingTracer) {
            val player = context.source.requirePlayer()

            val duration = Integer.MAX_VALUE
            val amplifier = 1
            val potionEffectType = PotionEffectType.NIGHT_VISION
            val potionEffect = PotionEffect(potionEffectType, duration, amplifier)

            val desiredState =
                runCatching {
                    context.getArgument("enabled", Boolean::class.java)
                }.getOrNull()

            val toggleOn =
                desiredState
                    ?: !player.hasPotionEffect(potionEffectType)

            player.removePotionEffect(potionEffectType)

            if (toggleOn) {
                player.addPotionEffect(potionEffect)
                player.sendRichMessage(l10n.nightVisionToggledOn)
            } else {
                player.sendRichMessage(l10n.nightVisionToggledOff)
            }
        }
}
