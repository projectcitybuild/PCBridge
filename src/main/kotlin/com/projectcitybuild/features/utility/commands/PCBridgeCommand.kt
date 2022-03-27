package com.projectcitybuild.features.utility.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.integrations.importers.PluginImporter
import com.projectcitybuild.integrations.importers.SpigotBannedIPImporter
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import java.util.Properties
import javax.inject.Inject

class PCBridgeCommand @Inject constructor(
    spigotBannedIPImporter: SpigotBannedIPImporter,
) : BungeecordCommand {

    override val label = "pcbridge"
    override val permission = "pcbridge.utilities"
    override val usageHelp = "/pcbridge"

    private val importers: HashMap<String, PluginImporter> = hashMapOf(
        Pair("banned-ips", spigotBannedIPImporter),
    )

    override suspend fun execute(input: BungeecordCommandInput) {
        when {
            input.args.isEmpty() -> showVersion(input.sender)
            input.args.first() == "import" -> import(input.sender, input.args)
            else -> throw InvalidCommandArgumentsException()
        }
    }

    private fun showVersion(sender: CommandSender) {
        val properties = Properties().apply {
            load(object {}.javaClass.getResourceAsStream("/version.properties"))
        }
        val version = properties.getProperty("version")
        val commit = properties.getProperty("commit")

        sender.send().info("Running PCBridge v$version ($commit)")
    }

    private fun import(sender: CommandSender, args: List<String>) {
        if (args.size <= 1) throw InvalidCommandArgumentsException()

        val name = args[1]
        val importer = importers[name]
        if (importer == null) {
            sender.send().error("Invalid import. Acceptable imports: ${importers.keys.joinToString(separator = ", ")}")
            return
        }
        importer.run()
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("import")
            args.size == 1 -> importers.keys
            else -> null
        }
    }
}
