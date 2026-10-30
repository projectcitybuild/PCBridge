package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeResolution
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeService
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.features.gameplay.time.GameTime
import com.projectcitybuild.pcbridge.features.gameplay.time.WorldTimeCommandInputParser
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandBuilder
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandDefinition
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TimeCommand(
    plugin: Plugin,
    private val timeService: WorldTimeService,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val timeArg =
        Argument.GameTime(
            "time",
            allowFixedTime = false,
            suggestionsAfterTime = timeService::worldNames,
        )

    override val builder: CommandDefinition = command("time") {
        description = l10n.gameplay.timeCommandDescription
        requiresPermission(GameplayPermissions.TIME)
        executesSuspending(taskExecutor, ::showTime)
        operation("set", Operation.SET)
        operation("add", Operation.ADD)
        argument(timeArg) {
            executesSuspending(taskExecutor) { changeTime(it, Operation.SET) }
        }
    }

    private fun CommandBuilder.operation(
        name: String,
        operation: Operation,
    ) {
        literal(name) {
            argument(timeArg) {
                executesSuspending(taskExecutor) { changeTime(it, operation) }
            }
        }
    }

    private suspend fun showTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val sender = commandContext.source.sender
        when (val resolution = timeService.resolveWorlds(null, (sender as? Player)?.world)) {
            is WorldTimeResolution.Found -> {
                resolution.worlds.forEach { world ->
                    sender.sendMessage(
                        l10n.gameplay.worldTimeCurrent(world.name, GameTime.formatTicks(timeService.current(world))),
                    )
                }
            }
            is WorldTimeResolution.Missing -> {
                sender.sendMessage(l10n.gameplay.errorWorldNotFound(resolution.name))
            }
        }
    }

    private suspend fun changeTime(
        context: PaperCommandContext,
        operation: Operation,
    ) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val rawInput = timeArg.get(commandContext).input
        val request = WorldTimeCommandInputParser.parse(rawInput) { input ->
            parseTicks(input, operation) != null
        }
        val input = request.time
        val ticks =
            when (operation) {
                Operation.SET -> GameTime.parseTicks(input)
                Operation.ADD -> GameTime.parseAddTicks(input)
            }
        if (ticks == null) {
            commandContext.source.sender.sendMessage(l10n.gameplay.errorInvalidTime(input))
            return@runCommandTracedSafely
        }

        val sender = commandContext.source.sender
        val resolution = timeService.resolveWorlds(request.worldSelector, (sender as? Player)?.world)
        when (resolution) {
            is WorldTimeResolution.Found -> {
                resolution.worlds.forEach { world ->
                    when (operation) {
                        Operation.ADD -> timeService.add(world, ticks)
                        Operation.SET -> timeService.set(world, ticks)
                    }
                    sender.sendMessage(
                        when (operation) {
                            Operation.ADD -> l10n.gameplay.worldTimeAdded(world.name, GameTime.formatTickAmount(ticks))
                            Operation.SET -> l10n.gameplay.worldTimeSet(world.name, GameTime.formatTicks(ticks))
                        },
                    )
                }
            }
            is WorldTimeResolution.Missing -> {
                sender.sendMessage(l10n.gameplay.errorWorldNotFound(resolution.name))
            }
        }
    }

    private fun parseTicks(
        input: String,
        operation: Operation,
    ): Long? = when (operation) {
        Operation.ADD -> GameTime.parseAddTicks(input)
        Operation.SET -> GameTime.parseTicks(input)
    }

    private enum class Operation {
        SET,
        ADD,
    }
}
