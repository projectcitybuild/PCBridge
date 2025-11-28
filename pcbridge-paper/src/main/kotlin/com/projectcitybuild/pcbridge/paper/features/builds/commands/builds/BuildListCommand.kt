package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pagination.PageComponentBuilder
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.buildsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin
import kotlin.math.ceil

class BuildListCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("list")
            .requiresPermission(PermissionNode.BUILDS_TELEPORT)
            .executesSuspending(plugin, ::execute)
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(buildsTracer) {
        val pageNumber = context.getOptionalArgument("page", Int::class.java) ?: 1
        val builds = buildRepository.all(pageNumber)
        val totalPages = ceil(builds.total.toDouble() / builds.perPage.toDouble()).toInt()
        val sender = context.source.sender

        if (builds.data.isEmpty()) {
            sender.sendRichMessage(
                if (pageNumber == 1) "<gray>No builds found</gray>"
                else l10n.errorPageNotFound
            )
            return@scoped
        }
        val message = PageComponentBuilder().build(
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