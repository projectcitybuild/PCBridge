package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.projectcitybuild.pcbridge.paper.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.http.models.pcb.Warp
import com.projectcitybuild.pcbridge.paper.core.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.core.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.core.support.spigot.UnauthorizedCommandException
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class WarpCommand(
    private val warpRepository: WarpRepository,
    private val server: Server,
) : SpigotCommand<WarpCommand.Args> {
    override val label = "warp"

    override val usage = CommandHelpBuilder(usage = "/warp <name>")

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
        val world = server.getWorld(warp.world)
        checkNotNull(world) {
            "World ${warp.world} does not exist"
        }

        val location = warp.toLocation(world)

        server.pluginManager.callEvent(
            PlayerPreWarpEvent(player),
        )

        player.teleportAsync(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        ).await()

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
        args: Array<out String>,
    ): List<String>? {
        val warps = warpRepository.all()

        if (args.isEmpty()) {
            return warps.map { it.name }
        }
        return warps
            .filter { it.name.lowercase().startsWith(args.first().lowercase()) }
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

private fun Warp.toLocation(world: World) = Location(
    world,
    x,
    y,
    z,
    yaw,
    pitch,
)