package com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.core.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin
import kotlin.math.ceil

class BuildListCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.BUILDS_TELEPORT)
            .executesSuspending(taskExecutor, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(taskExecutor, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(buildsTracer) {
            val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
            val builds = buildRepository.all(pageNumber)
            val totalPages = ceil(builds.total.toDouble() / builds.perPage.toDouble()).toInt()
            val sender = context.source.sender

            if (builds.data.isEmpty()) {
                sender.sendMessage(
                    if (pageNumber == 1) {
                        l10n.errorNoBuildsFound
                    } else {
                        l10n.errorPageNotFound
                    },
                )
                return@scoped
            }
            val message =
                PageComponentBuilder().build(
                    title = "Build List",
                    items = builds.data,
                    pageNumber = pageNumber,
                    totalPages = totalPages,
                    pageCommand = { index -> "/builds list $index" },
                    itemClickCommand = { "/build ${it.name}" },
                    itemHover = { "Teleport to ${it.name}" },
                    itemDecorator = {
                        "<gray>#${it.id} \"<aqua>${it.name}</aqua>\" (<white>${it.votes}</white> votes)</gray>"
                    },
                )
            sender.sendMessage(message)
        }
}
