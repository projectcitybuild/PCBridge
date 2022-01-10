package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import com.projectcitybuild.platforms.spigot.environment.CommandResult
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SetHubCommand(
    private val plugin: Plugin
): SpigotCommand {

    override val label: String = "sethub"
    override val permission = "pcbridge.hub.set"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.args.isNotEmpty()) {
            return CommandResult.INVALID_INPUT
        }
        val player = input.sender as? Player
        if (player == null) {
            input.sender.send().error("Console cannot use this command")
            return CommandResult.EXECUTED
        }

        MessageToBungeecord(
            plugin = plugin,
            sender = player,
            subChannel = SubChannel.SET_HUB,
            params = arrayOf(
                player.world.name,
                player.location.x,
                player.location.y,
                player.location.z,
                player.location.pitch,
                player.location.yaw,
            )
        ).send()

        return CommandResult.EXECUTED
    }
}
