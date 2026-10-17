package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.building.buildingTracer
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.argumenttypes.OnOffArgument
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    internal var hasNightVision: (Player) -> Boolean = {
        it.hasPotionEffect(PotionEffectType.NIGHT_VISION)
    }
    internal var removeNightVision: (Player) -> Unit = {
        it.removePotionEffect(PotionEffectType.NIGHT_VISION)
    }
    internal var enableNightVision: (Player) -> Unit = {
        it.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1))
    }

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

    internal suspend fun execute(context: PaperCommandContext) =
        context.scoped(buildingTracer) {
            val player = context.source.requirePlayer()

            val desiredState =
                runCatching {
                    context.getArgument("enabled", Boolean::class.java)
                }.getOrNull()

            val toggleOn =
                desiredState
                    ?: !hasNightVision(player)

            removeNightVision(player)

            if (toggleOn) {
                enableNightVision(player)
                player.sendMessage(l10n.nightVisionToggledOn)
            } else {
                player.sendMessage(l10n.nightVisionToggledOff)
            }
        }
}
