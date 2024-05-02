package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import com.projectcitybuild.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class WarpCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val server: Server,
): SpigotCommand<WarpCommand.Args> {
    override val label = "warp"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        if (!sender.hasPermission("pcbridge.warp.teleport")) {
            throw UnauthorizedCommandException()
        }
        val player = sender as? Player
        checkNotNull (player) {
            "Only players can use this command"
        }
        val warp = warpRepository.get(name = args.warpName)
        checkNotNull (warp) {
            "Warp ${args.warpName} not found"
        }
        val world = server.getWorld(warp.location.worldName)
        checkNotNull (world) {
            "World $world does not exist"
        }
        val location = Location(
            world,
            warp.location.x,
            warp.location.y,
            warp.location.z,
            warp.location.yaw,
            warp.location.pitch,
        )
        player.teleport(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        )
    }

    data class Args(
        val warpName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun tryParse(args: List<String>): Args? {
                if (args.isEmpty()) {
                    return null
                }
                return Args(warpName = args[0])
            }
        }
    }
}