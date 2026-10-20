package com.projectcitybuild.pcbridge.features.homes.hooks.commands.homes

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.homes.domain.repositories.HomeRepository
import com.projectcitybuild.pcbridge.features.homes.homesTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class HomeLimitCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("limit") {
            description = "Checks your home limit"
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(homesTracer) {
        val player = context.source.requirePlayer()

        val limit = homeRepository.limit(player)
        if (limit.max == 0) {
            context.source.sender.sendMessage(l10n.homes.homesCannotCreate)
            return@scoped
        }

        context.source.sender.sendMessage(l10n.homes.homesLimit(limit.current, limit.max))
        if (limit.sources.isNotEmpty()) {
            val sources = limit.sources.map { it.key to it.value }
            context.source.sender.sendMessage(l10n.homes.homesLimitDivider)
            context.source.sender.sendMessage(l10n.homes.homesLimitRoles(sources))
        }
    }
}
