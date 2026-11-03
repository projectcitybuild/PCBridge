package com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.builds.BuildsPermissions
import com.projectcitybuild.pcbridge.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.features.builds.domain.data.EditableBuildField
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import org.bukkit.plugin.Plugin

class BuildSetCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("set") {
        description = "Updates a field on a build"
        requiresPermission(BuildsPermissions.MANAGE)
        argument(Argument.integer("id")) {
            this.argument(Argument.enumValue("field", EditableBuildField::class.java)) {
                executesSuspending(taskExecutor, ::execute)
                this.argument(Argument.greedyString("value")) {
                    executesSuspending(taskExecutor, ::execute)
                }
            }
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(buildsTracer) {
        val player = context.source.requirePlayer()
        val field = Argument.enumValue("field", EditableBuildField::class.java).get(context)
        val id = Argument.integer("id").get(context)
        val value = Argument.greedyString("value").getOptional(context) ?: ""

        buildRepository.set(
            id = id,
            player = player,
            field = field,
            value = value,
        )
        context.source.sender.sendMessage(l10n.builds.buildUpdated)
    }
}
