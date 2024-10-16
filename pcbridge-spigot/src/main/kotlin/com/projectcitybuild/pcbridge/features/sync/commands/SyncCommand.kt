package com.projectcitybuild.pcbridge.features.sync.commands

import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SyncCommand(
) : SpigotCommand<SyncCommand.Args> {
    override val label = "sync"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        check(sender is Player) {
            "Only players can use this command"
        }
        // TODO
    }

    data class Args(
        val finishSyncing: Boolean,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    return Args(finishSyncing = false)
                }
                if (args[0] != "finish") {
                    throw BadCommandUsageException()
                }
                return Args(finishSyncing = true)
            }
        }
    }
}
