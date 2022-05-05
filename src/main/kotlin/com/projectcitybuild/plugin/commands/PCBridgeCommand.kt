package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.features.utilities.usecases.DataImportUseCase
import com.projectcitybuild.features.utilities.usecases.GetVersionUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.command.CommandSender
import javax.inject.Inject

class PCBridgeCommand @Inject constructor(
    private val getVersion: GetVersionUseCase,
    private val dataImport: DataImportUseCase,
) : SpigotCommand {

    override val label = "pcbridge"
    override val permission = "pcbridge.utilities"
    override val usageHelp = "/pcbridge"

    override suspend fun execute(input: SpigotCommandInput) {
        when {
            input.args.isEmpty() -> showVersion(input.sender)
            input.args.first() == "import" -> dataImport.execute(sender = input.player, args = input.args)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    private fun showVersion(sender: CommandSender) {
        val version = getVersion.execute()
        sender.send().info("Running PCBridge v${version.version} (${version.commitHash})")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("import")
            args.size == 1 -> listOf("hub", "banned-ips")
            else -> null
        }
    }
}
