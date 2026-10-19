package com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.domain.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.WarpNameSuggester
import com.projectcitybuild.pcbridge.features.warps.hooks.dialogs.WarpRenameDialog
import com.projectcitybuild.pcbridge.features.warps.warpsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpRenameCommand(
    private val plugin: Plugin,
    private val warpNameSuggester: WarpNameSuggester,
    private val warpRepository: WarpRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("rename")
        .requiresPermission(WarpsPermissions.MANAGE)
        .then(
            Commands
                .argument("name", StringArgumentType.string())
                .suggestsSuspending(plugin, warpNameSuggester::suggest)
                .executesSuspending(taskExecutor, ::execute),
        ).build()

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warpsTracer) {
        val player = context.source.requirePlayer()
        val name = context.getArgument("name", String::class.java)

        val warp = warpRepository.get(name)
        checkNotNull(warp) { l10n.warps.errorWarpNotFound(name) }

        val dialog =
            WarpRenameDialog.build(
                warpId = warp.id,
                prevName = warp.name,
                newName = warp.name,
            )
        player.showDialog(dialog)
    }
}
