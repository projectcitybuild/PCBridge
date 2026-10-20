package com.projectcitybuild.pcbridge.features.builds.hooks.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.features.builds.BuildsPermissions
import com.projectcitybuild.pcbridge.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.features.builds.domain.repositories.BuildRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin
import kotlin.math.ceil

class BuildListCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("list") {
            description = "Lists available builds"
            requiresPermission(BuildsPermissions.TELEPORT)
            argument("page", IntegerArgumentType.integer()) {
                executesSuspending(taskExecutor, ::execute)
            }
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(buildsTracer) {
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val builds = buildRepository.all(pageNumber)
        val totalPages = ceil(builds.total.toDouble() / builds.perPage.toDouble()).toInt()
        val sender = context.source.sender

        if (builds.data.isEmpty()) {
            sender.sendMessage(
                if (pageNumber == 1) {
                    l10n.builds.errorNoBuildsFound
                } else {
                    l10n.pagination.errorPageNotFound
                },
            )
            return@scoped
        }
        val message =
            PageComponentBuilder().buildWithComponents(
                title = l10n.builds.buildListTitle,
                items = builds.data,
                pageNumber = pageNumber,
                totalPages = totalPages,
                pageCommand = { index -> "/builds list $index" },
                itemClickCommand = { "/build ${it.name}" },
                itemHover = { l10n.lists.teleportTo(it.name) },
                itemDecorator = { l10n.builds.buildListItem(it.id, it.name, it.votes) },
            )
        sender.sendMessage(message)
    }
}
