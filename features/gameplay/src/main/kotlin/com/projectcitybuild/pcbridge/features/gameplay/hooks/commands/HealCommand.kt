package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.Server
import org.bukkit.attribute.Attribute
import org.bukkit.plugin.Plugin

class HealCommand(
    plugin: Plugin,
    server: Server,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)

    override val builder = command("heal") {
        description = l10n.gameplay.healCommandDescription
        requiresPermission(GameplayPermissions.HEAL)
        argument(playerArg) {
            executesSuspending(taskExecutor, ::execute)
        }
        executesSuspending(taskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext)
            ?: commandContext.source.requirePlayer()

        val maxHealth = checkNotNull(target.getAttribute(Attribute.MAX_HEALTH)) {
            l10n.gameplay.errorPlayerMaxHealthUnavailable(target.name)
        }.value

        target.health = maxHealth
        target.fireTicks = 0

        commandContext.source.sender.sendMessage(l10n.gameplay.playerHealed(target.name))
    }
}
