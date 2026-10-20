package com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.domain.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class WarpDeleteCommand(
    private val plugin: Plugin,
    private val warpNameSuggester: WarpNameSuggester,
    private val warpRepository: WarpRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal() = command("delete") {
        requiresPermission(WarpsPermissions.MANAGE)
        argument("name", StringArgumentType.string()) {
            suggestsSuspending(plugin, warpNameSuggester::suggest)
                .executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val warpName = context.getArgument("name", String::class.java)

        warpRepository.delete(name = warpName)

        eventBroadcaster.broadcast(WarpDeleteEvent())

        context.source.sender.sendMessage(
            l10n.warps.warpDeleted(warpName),
        )
    }
}
