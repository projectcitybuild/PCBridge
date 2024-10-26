package com.projectcitybuild.pcbridge.features.warps.commands

import com.projectcitybuild.pcbridge.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.UnauthorizedCommandException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class WarpCommand(
    private val warpRepository: WarpRepository,
    private val server: Server,
) : SpigotCommand<WarpCommand.Args> {
    override val label = "warp"

    override val usage = CommandHelpBuilder(usage = "/warp <name>") // TODO

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        if (!sender.hasPermission("pcbridge.warp.teleport")) {
            throw UnauthorizedCommandException()
        }
        val player = sender as? Player
        checkNotNull(player) {
            "Only players can use this command"
        }
        val warp = warpRepository.get(name = args.warpName)
        checkNotNull(warp) {
            "Warp ${args.warpName} not found"
        }
        val world = server.getWorld(warp.location.worldName)
        checkNotNull(world) {
            "World $world does not exist"
        }
        val location =
            Location(
                world,
                warp.location.x,
                warp.location.y,
                warp.location.z,
                warp.location.yaw,
                warp.location.pitch,
            )
        server.pluginManager.callEvent(
            PlayerPreWarpEvent(player),
        )
        player.teleport(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        )
        sender.sendMessage(
            Component.text("Warped to ${warp.name}")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC),
        )
    }

    override suspend fun tabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Args
    ): List<String>? {
        if (args.warpName.isEmpty()) return null

        return warpRepository.all()
            .filter { it.name.lowercase().startsWith(args.warpName.lowercase()) }
            .map { it.name }
    }

    data class Args(
        val warpName: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(warpName = args[0])
            }
        }
    }
}
