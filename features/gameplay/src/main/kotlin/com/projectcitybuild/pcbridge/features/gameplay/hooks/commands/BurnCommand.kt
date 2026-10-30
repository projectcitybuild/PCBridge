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
import org.bukkit.plugin.Plugin

class BurnCommand(
    plugin: Plugin,
    server: Server,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)
    private val secondsArg = Argument.Integer("seconds", min = 1, max = MAX_DURATION_SECONDS)

    override val builder = command("burn") {
        description = l10n.gameplay.burnCommandDescription
        requiresPermission(GameplayPermissions.BURN)
        argument(playerArg) {
            argument(secondsArg) {
                executesSuspending(taskExecutor, ::execute)
            }
            executesSuspending(taskExecutor, ::execute)
        }
        argument(secondsArg) {
            executesSuspending(taskExecutor, ::execute)
        }
        executesSuspending(taskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext) ?: commandContext.source.requirePlayer()
        val seconds = secondsArg.getOptional(commandContext) ?: DEFAULT_DURATION_SECONDS
        target.fireTicks = seconds * TICKS_PER_SECOND
        commandContext.source.sender.sendMessage(l10n.gameplay.playerSetOnFire(target.name))
    }

    private companion object {
        const val DEFAULT_DURATION_SECONDS = 5
        const val MAX_DURATION_SECONDS = Int.MAX_VALUE / 20
        const val TICKS_PER_SECOND = 20
    }
}
