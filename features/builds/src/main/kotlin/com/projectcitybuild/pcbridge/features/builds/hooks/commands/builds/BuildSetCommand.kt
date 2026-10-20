package com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.builds.BuildsPermissions
import com.projectcitybuild.pcbridge.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.features.builds.domain.data.EditableBuildField
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.EnumArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class BuildSetCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("set") {
        description = "Updates a field on a build"
        requiresPermission(BuildsPermissions.MANAGE)
        argument("id", IntegerArgumentType.integer()) {
            this.argument("field", EnumArgument(EditableBuildField::class.java)) {
                executesSuspending(taskExecutor, ::execute)
                this.argument("value", StringArgumentType.greedyString()) {
                    executesSuspending(taskExecutor, ::execute)
                }
            }
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(buildsTracer) {
        val player = context.source.requirePlayer()
        val field = context.getArgument("field", EditableBuildField::class.java)
        val id = context.getArgument("id", Int::class.java)
        val value = context.getOptionalArgument("value", String::class.java) ?: ""

        buildRepository.set(
            id = id,
            player = player,
            field = field,
            value = value,
        )
        context.source.sender.sendMessage(l10n.builds.buildUpdated)
    }
}
