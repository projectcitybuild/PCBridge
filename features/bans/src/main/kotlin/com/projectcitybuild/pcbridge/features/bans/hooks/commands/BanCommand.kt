package com.projectcitybuild.pcbridge.features.bans.hooks.commands

import com.projectcitybuild.pcbridge.features.bans.BansPermissions
import com.projectcitybuild.pcbridge.features.bans.bansTracer
import com.projectcitybuild.pcbridge.features.bans.hooks.dialogs.CreateBanDialog
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.runtime.commands.runCommandTracedSafely
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val createDialog: (String?) -> DialogLike = CreateBanDialog::build,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("ban") {
        description = "Bans a player from the server"
        requiresPermission(BansPermissions.MANAGE)
        argument("player", OnlinePlayerNameArgument(server)) {
            executesSuspending(taskExecutor, ::execute)
        }
    }

    private suspend fun execute(context: PaperCommandContext) = context.runCommandTracedSafely(bansTracer) {
        val inputPlayerName = context.getArgument("player", String::class.java)

        val playerName =
            inputPlayerName
                ?.let { name -> server.onlinePlayer(name = name)?.name }
                ?: inputPlayerName

        val dialog = createDialog(playerName)
        context.source.sender.showDialog(dialog)
    }
}
