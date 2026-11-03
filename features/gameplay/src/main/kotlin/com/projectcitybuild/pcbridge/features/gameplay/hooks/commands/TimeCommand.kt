package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeService
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
import com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments.GameTimeArgument
import com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments.WorldSelectorArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.Server
import org.bukkit.entity.Player

class TimeCommand(
    server: Server,
    private val timeService: WorldTimeService,
    private val taskExecutor: CommandTaskExecutor,
) : BrigadierCommand {
    private val timeArg = GameTimeArgument(
        "time",
        allowFixedTime = false,
        suggestionsAfterTime = timeService::worldNames,
    )
    private val worldArg = Argument.World(name = "world")
    private val worldSelectorArg = WorldSelectorArgument(name = "world", server)

    override val builder = command("time") {
        description = l10n.gameplay.timeCommandDescription
        requiresPermission(GameplayPermissions.TIME)
        executesSuspending(taskExecutor, ::getTime)
        argument(worldArg) {
            executesSuspending(taskExecutor, ::getTime)
        }
        argument(timeArg) {
            executesSuspending(taskExecutor, ::setTime)
            argument(worldSelectorArg) {
                executesSuspending(taskExecutor, ::setTime)
            }
        }
    }

    private suspend fun getTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val sender = commandContext.source.sender
        val world = worldArg.getOptional(context)
            ?: (sender as? Player)?.world

        if (world == null) {
            sender.sendMessage(l10n.gameplay.errorTimeWorldRequired)
            return@runCommandTracedSafely
        }
        sender.sendMessage(
            l10n.gameplay.worldTimeCurrent(world.name, formatTicks(world.time)),
        )
    }

    private suspend fun setTime(context: PaperCommandContext) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val sender = commandContext.source.sender
        val input = timeArg.get(commandContext).input
        val worldSelector = worldSelectorArg.getOptional(context)
            ?: WorldSelector.Current

        when (
            val result = timeService.set(
                input = input,
                selector = worldSelector,
                defaultWorld = (sender as? Player)?.world,
            )
        ) {
            is WorldTimeService.SetResult.InvalidTime -> {
                sender.sendMessage(l10n.gameplay.errorInvalidTime(result.input))
            }
            is WorldTimeService.SetResult.WorldNotFound -> {
                sender.sendMessage(l10n.gameplay.errorWorldNotFound(result.name))
            }
            is WorldTimeService.SetResult.WorldRequired -> {
                sender.sendMessage(l10n.gameplay.errorTimeWorldRequired)
            }
            is WorldTimeService.SetResult.Updated -> {
                result.changes.forEach { change ->
                    sender.sendMessage(
                        change.relativeTicks?.let { ticks ->
                            l10n.gameplay.worldTimeAdded(change.world.name, "$ticks ticks")
                        } ?: l10n.gameplay.worldTimeSet(change.world.name, formatTicks(change.time)),
                    )
                }
            }
        }
    }

    private fun formatTicks(ticks: Long) = "${ticks.mod(TICKS_PER_DAY)} ticks"

    private companion object {
        const val TICKS_PER_DAY = 24_000L
    }
}
