package com.projectcitybuild.commands

import com.projectcitybuild.features.utilities.usecases.GetVersion
import com.projectcitybuild.features.utilities.usecases.ReloadPlugin
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.command.CommandSender

class PCBridgeCommand(
    private val getVersion: GetVersion,
    private val reloadPlugin: ReloadPlugin,
) : SpigotCommand {

    override val label = "pcbridge"
    override val permission = "pcbridge.utilities"
    override val usageHelp = "/pcbridge"

    override suspend fun execute(input: SpigotCommandInput) {
        when {
            input.args.isEmpty() -> showVersion(input.sender)
            input.args.first() == "reload" -> reloadPlugin(input)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    private fun showVersion(sender: CommandSender) {
        val version = getVersion.execute()
        sender.send().info("Running PCBridge v${version.version} (${version.commitHash})")
    }

    private fun reloadPlugin(input: SpigotCommandInput) {
        reloadPlugin.execute()
        input.sender.send().success("Caches flushed")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("import", "reload")
            args.size == 1 -> listOf("hub", "banned-ips")
            else -> null
        }
    }
}
