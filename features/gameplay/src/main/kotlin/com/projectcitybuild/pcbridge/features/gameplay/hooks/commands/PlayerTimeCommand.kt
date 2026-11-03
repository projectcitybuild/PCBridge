package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerTimeService
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments.GameTimeArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class PlayerTimeCommand(
    plugin: Plugin,
    private val server: Server,
    private val playerTimeService: PlayerTimeService,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)
    private val playerNameArg = Argument.OnlinePlayerName("player", server)
    private val timeArg = GameTimeArgument(
        "time",
        allowFixedTime = true,
        suggestionsAfterTime = { server.onlinePlayers.map { it.name } },
    )

    override val builder = command("ptime") {
        description = l10n.gameplay.playerTimeCommandDescription
        requiresPermission(GameplayPermissions.PLAYER_TIME)
        executesSuspending(taskExecutor, ::showTime)
        literal("get") {
            argument(playerArg) {
                executesSuspending(taskExecutor, ::showTime)
            }
            executesSuspending(taskExecutor, ::showTime)
        }
        literal("reset") {
            argument(playerArg) {
                executesSuspending(taskExecutor) { resetTime(it) }
            }
            executesSuspending(taskExecutor, ::resetTime)
        }
        argument(timeArg) {
            executesSuspending(taskExecutor, ::setTime)
            argument(playerNameArg) {
                executesSuspending(taskExecutor, ::setTime)
            }
        }
    }

    private suspend fun showTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext)
            ?: commandContext.source.requirePlayer()

        val isInSync = target.isPlayerTimeRelative && target.playerTimeOffset == 0L
        val message = if (isInSync) {
            l10n.gameplay.playerTimeNormal(target.name)
        } else {
            l10n.gameplay.playerTimeCurrent(
                target.name,
                formatTicks(target.playerTime),
                target.isPlayerTimeRelative,
            )
        }
        commandContext.source.sender.sendMessage(message)
    }

    private suspend fun setTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val playerName = playerNameArg.getOptional(commandContext)
        val timeInput = timeArg.get(commandContext).input
        val rawInput = playerName?.let { "$timeInput $it" } ?: timeInput
        val target = playerName?.let { name ->
            server.onlinePlayer(name, ignoreCase = false) ?: run {
                if (!playerTimeService.isValid(timeInput)) {
                    commandContext.source.sender.sendMessage(l10n.gameplay.errorInvalidTime(rawInput))
                    return@runCommandTracedSafely
                }
                commandContext.source.sender.sendMessage(l10n.errors.errorPlayerNotFound(name))
                return@runCommandTracedSafely
            }
        } ?: commandContext.source.requirePlayer()

        when (val result = playerTimeService.set(target, timeInput)) {
            is PlayerTimeService.SetResult.InvalidTime -> {
                commandContext.source.sender.sendMessage(l10n.gameplay.errorInvalidTime(rawInput))
            }
            is PlayerTimeService.SetResult.Updated -> {
                commandContext.source.sender.sendMessage(
                    l10n.gameplay.playerTimeSet(
                        target.name,
                        formatTicks(result.ticks),
                        result.relative,
                    ),
                )
            }
        }
    }

    private suspend fun resetTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext)
            ?: commandContext.source.requirePlayer()

        target.resetPlayerTime()

        commandContext.source.sender.sendMessage(
            l10n.gameplay.playerTimeReset(target.name),
        )
    }

    private fun formatTicks(ticks: Long) = "${ticks.mod(TICKS_PER_DAY)} ticks"

    private companion object {
        const val TICKS_PER_DAY = 24_000L
    }
}
