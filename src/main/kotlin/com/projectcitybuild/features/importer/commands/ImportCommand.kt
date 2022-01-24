package com.projectcitybuild.features.importer.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
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

    override val label = "importpcb"
    override val permission = "pcbridge.import"
    override val usageHelp = "/importpcb"

    override suspend fun execute(input: BungeecordCommandInput) {
        when {
            input.args.isEmpty() -> throw InvalidCommandArgumentsException()
            else -> throw InvalidCommandArgumentsException()
        }
        input.sender.send().success("Migration complete")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf()
            else -> null
        }
    }
}
