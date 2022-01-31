package com.projectcitybuild.features.importer.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.hub.HubFileStorage
import com.projectcitybuild.modules.database.DataSource
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Plugin
import javax.inject.Inject

class ImportCommand @Inject constructor(
    private val plugin: Plugin,
    private val dataSource: DataSource,
): BungeecordCommand {

    override val label = "pcbridge"
    override val permission = "pcbridge.utilities"
    override val usageHelp = "/pcbridge"

    override suspend fun execute(input: BungeecordCommandInput) {
        when {
            input.args.isEmpty() -> throw InvalidCommandArgumentsException()
            input.args.first() == "import" -> import(input.sender, input.args)
            else -> throw InvalidCommandArgumentsException()
        }
        input.sender.send().success("Migration complete")
    }

    private fun import(sender: CommandSender, args: List<String>) {
        if (args.size <= 1) throw InvalidCommandArgumentsException()

        val name = args[1]
        when (name) {
            "hub" -> {
                val storage = HubFileStorage(plugin.dataFolder)
                val hub = storage.load()

                if (hub != null) {
                    dataSource.database().executeInsert(
                        "INSERT INTO `hub` VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        hub.serverName,
                        hub.worldName,
                        hub.x,
                        hub.y,
                        hub.z,
                        hub.pitch,
                        hub.yaw,
                        hub.createdAt.unwrapped
                    )
                } else {
                    sender.send().error("Hub not found")
                }
            }
            else -> sender.send().error("Invalid import")
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf()
            else -> null
        }
    }
}
