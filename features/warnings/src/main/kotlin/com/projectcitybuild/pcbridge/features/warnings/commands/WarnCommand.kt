package com.projectcitybuild.pcbridge.features.warnings.commands

import com.projectcitybuild.pcbridge.features.warnings.WarningsPermissions
import com.projectcitybuild.pcbridge.features.warnings.warningsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class WarnCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val remoteConfig: RemoteConfig,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("warn")
        .requiresPermission(WarningsPermissions.MANAGE)
        .then(
            Commands
                .argument("player", OnlinePlayerNameArgument(server))
                .executesSuspending(taskExecutor, ::execute),
        ).build()

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warningsTracer) {
        val playerName = context.getArgument("player", String::class.java)

        val player = server.onlinePlayer(name = playerName)
        val lookup = player?.uniqueId?.toString() ?: playerName

        val manageBaseUrl = remoteConfig.latest.config.manageBaseUrl
        val url = manageBaseUrl + "warnings/create?uuid=$lookup"

        val sender = context.source.sender
        sender.sendRichMessage(
            "<gray>Click the link below to create a warning for this player</gray>",
        )
        sender.sendRichMessage(
            "<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>",
        )
    }
}
