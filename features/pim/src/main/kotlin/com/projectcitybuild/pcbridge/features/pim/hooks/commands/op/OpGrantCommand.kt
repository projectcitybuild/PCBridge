package com.projectcitybuild.pcbridge.features.pim.hooks.commands.op

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.support.java.humanReadable
import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import com.projectcitybuild.pcbridge.features.pim.hooks.dialogs.ConfirmOpElevateDialog
import com.projectcitybuild.pcbridge.features.pim.pimTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.plugin.Plugin

class OpGrantCommand(
    private val plugin: Plugin,
    private val opElevationService: OpElevationService,
    private val localizedTime: LocalizedTime,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    internal var confirmationDialog: () -> DialogLike = ConfirmOpElevateDialog::build

    override fun literal(): PaperCommandNode = command("grant") {
        requiresPermission(PimPermissions.OP)
        executesSuspending(commandTaskExecutor, ::execute)
    }

    suspend fun execute(context: PaperCommandContext) = context.scoped(pimTracer) {
        val player = context.source.requirePlayer()

        val elevation = opElevationService.elevation(player.uniqueId)
        val now = localizedTime.nowInstant()
        val isActive = elevation != null && elevation.isActiveAt(now)
        if (isActive) {
            player.sendMessage(l10n.pim.opElevationAlreadyActive(elevation?.remainingAt(now)?.humanReadable()))
            return@scoped
        }

        val dialog = confirmationDialog()
        player.showDialog(dialog)
    }
}
