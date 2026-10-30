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
import org.checkerframework.checker.units.qual.t

class KillCommand(
    plugin: Plugin,
    server: Server,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)

    override val builder = command("kill") {
        description = l10n.gameplay.killCommandDescription
        requiresPermission(GameplayPermissions.KILL)
        argument(playerArg) {
            executesSuspending(taskExecutor, ::execute)
        }
        executesSuspending(taskExecutor, ::execute)
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext)
            ?: commandContext.source.requirePlayer()

        target.health = 0.0

        commandContext.source.sender.sendMessage(
            l10n.gameplay.playerKilled(target.name),
        )
        if (target != commandContext.source.sender) {
            target.sendMessage(l10n.gameplay.killedBy(commandContext.source.sender.name))
        }
    }
}
