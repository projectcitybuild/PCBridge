package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.time.LocalDateTime
import javax.inject.Inject

class SetHubCommand @Inject constructor(
    private val plugin: Plugin
): SpigotCommand {

    override val label = "sethub"
    override val permission = "pcbridge.hub.set"
    override val usageHelp = "/sethub"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        val player = input.sender as? Player
        if (player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val warp = Warp(
            "hub",
            CrossServerLocation(
                serverName,
                worldName,
                x,
                y,
                z,
                pitch,
                yaw,
            ),
            LocalDateTime.now(),
        )
        hubRepository.save(warp, receiver.uniqueId)

        receiver.send().success("Destination of /hub has been set")
    }
}
