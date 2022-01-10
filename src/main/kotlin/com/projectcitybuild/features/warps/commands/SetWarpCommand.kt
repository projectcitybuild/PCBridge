package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SetWarpCommand(
    private val plugin: Plugin
): SpigotCommand {

    override val label = "setwarp"
    override val permission = "pcbridge.warp.create"
    override val usageHelp = "/setwarp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }
        val player = input.sender as? Player
        if (player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val warpName = input.args.first()

        MessageToBungeecord(
            plugin = plugin,
            sender = player,
            subChannel = SubChannel.SET_WARP,
            params = arrayOf(
                warpName,
                player.world.name,
                player.location.x,
                player.location.y,
                player.location.z,
                player.location.pitch,
                player.location.yaw,
            )
        ).send()
    }
}
