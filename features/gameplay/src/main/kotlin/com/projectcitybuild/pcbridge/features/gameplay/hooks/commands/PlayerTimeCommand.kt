package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.PlayerTimeService
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.features.gameplay.time.GameTime
import com.projectcitybuild.pcbridge.features.gameplay.time.PlayerTimeCommandInputParser
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandDefinition
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
    private val timeArg =
        Argument.GameTime(
            "time",
            allowFixedTime = true,
            suggestionsAfterTime = { server.onlinePlayers.map { it.name } },
        )

    override val builder: CommandDefinition = command("ptime") {
        description = l10n.gameplay.playerTimeCommandDescription
        requiresPermission(GameplayPermissions.PLAYER_TIME)
        executesSuspending(taskExecutor, ::showTime)
        showOperation("get", ::showTime)
        resetOperation("reset")
        resetOperation("normal")
        resetOperation("default")
        argument(timeArg) {
            executesSuspending(taskExecutor, ::setTime)
        }
    }

    private fun CommandBuilder.showOperation(
        name: String,
        execute: suspend (PaperCommandContext) -> Unit,
    ) {
        literal(name) {
            argument(playerArg) {
                executesSuspending(taskExecutor, execute)
            }
            executesSuspending(taskExecutor, execute)
        }
    }

    private fun CommandBuilder.resetOperation(name: String) {
        literal(name) {
            argument(playerArg) {
                executesSuspending(taskExecutor) { resetTime(it) }
            }
            executesSuspending(taskExecutor, ::resetTime)
        }
    }

    private suspend fun showTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext)
            ?: commandContext.source.requirePlayer()

        val state = playerTimeService.current(target)
        val message = state?.let {
            l10n.gameplay.playerTimeCurrent(
                name = target.name,
                time = GameTime.formatTicks(it.ticks),
                relative = it.relative,
            )
        } ?: l10n.gameplay.playerTimeNormal(target.name)
        commandContext.source.sender.sendMessage(message)
    }

    private suspend fun setTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val request = PlayerTimeCommandInputParser.parse(
            timeArg.get(commandContext).input,
            server.onlinePlayers.map { it.name }.toSet(),
        )
        val input = request.time
        val parsedTime = GameTime.parsePlayerTime(input)
        if (parsedTime == null) {
            commandContext.source.sender.sendMessage(l10n.gameplay.errorInvalidTime(input))
            return@runCommandTracedSafely
        }

        val target = request.playerName?.let { playerName ->
            server.onlinePlayer(playerName, ignoreCase = false) ?: run {
                commandContext.source.sender.sendMessage(l10n.errors.errorPlayerNotFound(playerName))
                return@runCommandTracedSafely
            }
        } ?: run {
            commandContext.source.requirePlayer()
        }

        playerTimeService.set(target, parsedTime.ticks, parsedTime.relative)

        commandContext.source.sender.sendMessage(
            l10n.gameplay.playerTimeSet(target.name, GameTime.formatTicks(parsedTime.ticks), parsedTime.relative),
        )
    }

    private suspend fun resetTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext)
            ?: commandContext.source.requirePlayer()

        playerTimeService.reset(target)

        commandContext.source.sender.sendMessage(
            l10n.gameplay.playerTimeReset(target.name),
        )
    }
}
