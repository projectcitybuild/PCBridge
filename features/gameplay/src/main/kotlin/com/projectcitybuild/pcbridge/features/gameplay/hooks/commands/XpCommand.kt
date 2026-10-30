package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import com.projectcitybuild.pcbridge.features.gameplay.gameplayTracer
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
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import kotlin.math.absoluteValue

class XpCommand(
    plugin: Plugin,
    server: Server,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private val playerArg = Argument.OnlinePlayer("player", server)
    private val amountArg = Argument.Integer("amount", min = 0)
    private val legacyAmountArg = Argument.Integer("amount")

    override val builder: CommandDefinition = command("xp") {
        description = l10n.gameplay.xpCommandDescription
        requiresPermission(GameplayPermissions.XP)
        argument(legacyAmountArg) {
            argument(playerArg) {
                executesSuspending(taskExecutor) { execute(it, legacyAmountArg, Operation.ADD) }
            }
            executesSuspending(taskExecutor) { execute(it, legacyAmountArg, Operation.ADD) }
        }
        operation("set", Operation.SET)
        operation("add", Operation.ADD)
        operation("subtract", Operation.SUBTRACT)
        operation("reset", Operation.RESET)
    }

    private fun CommandBuilder.operation(
        name: String,
        operation: Operation,
    ) {
        literal(name) {
            if (operation == Operation.RESET) {
                argument(playerArg) {
                    executesSuspending(taskExecutor) { execute(it, null, operation) }
                }
                executesSuspending(taskExecutor) { execute(it, null, operation) }
            } else {
                argument(amountArg) {
                    argument(playerArg) {
                        executesSuspending(taskExecutor) { execute(it, amountArg, operation) }
                    }
                    executesSuspending(taskExecutor) { execute(it, amountArg, operation) }
                }
            }
        }
    }

    private suspend fun execute(
        context: PaperCommandContext,
        amount: Argument.Integer?,
        operation: Operation,
    ) = context.runCommandTracedSafely(gameplayTracer) { commandContext ->
        val target = playerArg.getOptional(commandContext) ?: commandContext.source.requirePlayer()
        val experience = amount?.get(commandContext) ?: 0
        val effectiveOperation = if (operation == Operation.ADD && experience < 0) Operation.SUBTRACT else operation
        when (operation) {
            Operation.SET -> {
                target.exp = 0f
                target.level = 0
                target.totalExperience = 0
                target.giveExp(experience)
            }
            Operation.ADD -> {
                target.giveExp(experience)
            }
            Operation.SUBTRACT -> {
                target.giveExp(-experience)
            }
            Operation.RESET -> {
                target.exp = 0f
                target.level = 0
                target.totalExperience = 0
            }
        }
        val currentExperience = target.totalExperience
        val operationAmount = experience.toLong().absoluteValue
        val message = when (effectiveOperation) {
            Operation.SET -> l10n.gameplay.playerExperienceSet(target.name, currentExperience)
            Operation.ADD -> l10n.gameplay.playerExperienceAdded(target.name, operationAmount, currentExperience)
            Operation.SUBTRACT -> l10n.gameplay.playerExperienceSubtracted(target.name, operationAmount, currentExperience)
            Operation.RESET -> l10n.gameplay.playerExperienceReset(target.name)
        }
        commandContext.source.sender.sendMessage(message)
    }

    private enum class Operation {
        SET,
        ADD,
        SUBTRACT,
        RESET,
    }
}
