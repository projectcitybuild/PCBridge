package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandDefinition
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class PurgeCommand(
    plugin: Plugin,
    server: Server,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)

    override val builder: CommandDefinition = command("purge") {
        description = l10n.gameplay.purgeCommandDescription
        requiresPermission(GameplayPermissions.PURGE)
        argument(playerArg) {
            executesSuspending(taskExecutor, ::execute)
        }
        executesSuspending(taskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext) ?: commandContext.source.requirePlayer()
        val numActivePotions = target.activePotionEffects.size
        target.activePotionEffects.toList().forEach { effect ->
            target.removePotionEffect(effect.type)
        }
        if (numActivePotions == 0) {
            commandContext.source.sender.sendMessage(l10n.gameplay.noActivePotionEffects)
            return@runCommandTracedSafely
        }
        commandContext.source.sender.sendMessage(
            l10n.gameplay.playerEffectsPurged(numActivePotions, target.name),
        )
    }
}
