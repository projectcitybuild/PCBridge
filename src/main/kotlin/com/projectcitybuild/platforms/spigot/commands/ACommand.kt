package com.projectcitybuild.platforms.spigot.commands

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.SubChannel
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ACommand(
    private val plugin: Plugin
): Commandable {

    override val label: String = "a"
    override val permission: String = "pcbridge.chat.staff_channel.send"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.args.isEmpty())
            return CommandResult.INVALID_INPUT

        val message = input.args.joinWithWhitespaces(1 until input.args.size)

        val out = ByteStreams.newDataOutput()
        out.writeUTF(SubChannel.STAFF_CHAT)
        out.writeUTF(message)

        val player = input.sender as Player
        player.sendPluginMessage(plugin, Channel.BUNGEECORD, out.toByteArray())

        return CommandResult.EXECUTED
    }
}
