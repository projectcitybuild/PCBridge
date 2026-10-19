package com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.domain.events.WarpCreateEvent
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpCreateCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("create")
        .requiresPermission(WarpsPermissions.MANAGE)
        .then(
            Commands
                .argument("name", StringArgumentType.string())
                .executesSuspending(taskExecutor, ::execute),
        ).build()

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val player = context.source.requirePlayer()
        val warpName = context.getArgument("name", String::class.java)

        val warp =
            warpRepository.create(
                name = warpName,
                location = player.location,
            )

        eventBroadcaster.broadcast(WarpCreateEvent())

        player.sendMessage(l10n.warpCreated(warp.name))
    }
}
