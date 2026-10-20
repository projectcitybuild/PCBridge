package com.projectcitybuild.pcbridge.features.building.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.building.BuildingPermissions
import com.projectcitybuild.pcbridge.features.building.buildingTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnOffArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NightVisionCommand(
    private val plugin: Plugin,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
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

    override fun literal() = command("nv") {
        requiresPermission(BuildingPermissions.NIGHT_VISION)
        argument("enabled", OnOffArgument()) {
            executesSuspending(commandTaskExecutor, ::execute)
        }
        executesSuspending(commandTaskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(buildingTracer) {
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
            player.sendMessage(l10n.building.nightVisionToggledOn)
        } else {
            player.sendMessage(l10n.building.nightVisionToggledOff)
        }
    }
}
