package com.projectcitybuild.pcbridge.paper.features.teleport.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.warps.events.PlayerPreWarpEvent
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldBorder
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import java.lang.Double.min
import kotlin.math.max
import kotlin.random.Random

class RtpCommand(
    private val plugin: Plugin,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("rtp")
            .requiresPermission(PermissionNode.TELEPORT_RANDOM)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val miniMessage = MiniMessage.miniMessage()
        val executor = context.source.executor
        val player = executor as? Player
        checkNotNull(player) { "Only players can use this command" }

        executor.sendMessage(
            miniMessage.deserialize("<gray><italic>Teleporting...</italic></gray>")
        )

        val world = player.location.world
        val location = safeLocation(world)
        if (location == null) {
            executor.sendMessage(
                miniMessage.deserialize("<red>Failed to find a safe location</red>")
            )
            return@traceSuspending
        }

        world.getChunkAtAsyncUrgently(location).await()

        eventBroadcaster.broadcast(
            // TODO: use a non-warp related event
            PlayerPreWarpEvent(player)
        )
        player.teleportAsync(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        ).await()

        executor.sendMessage(
            miniMessage.deserialize("<green>Teleported to </green><gray>x=${location.x}, y=${location.y}, z=${location.z}</gray>")
        )
    }

    private fun safeLocation(world: World): Location? {
        val bounds = world.worldBorder
        val center = bounds.center
        val size = bounds.size

        // If a WorldBorder is not set, the API still returns an enormous bounds
        // (60 mil in both axis) so we need to always clamp this
        val xRange = (center.x - size).coerceAtLeast(-15_000.0).toInt()..
            (center.x + size).coerceAtMost(15_000.0).toInt()
        val zRange = (center.z - size).coerceAtLeast(-15_000.0).toInt()..
            (center.z + size).coerceAtMost(15_000.0).toInt()

        for (i in 1..5) {
            val x = xRange.random()
            val z = zRange.random()
            val location = safeYLocation(world, x, z)
            if (location != null) return location
        }
        return null
    }

    private fun safeYLocation(world: World, x: Int, z: Int): Location? {
        val highestBlock = world.getHighestBlockAt(x, z)
        val aboveBlock = world.getBlockAt(x, highestBlock.y + 1, z)

        // Need headroom to teleport
        if (aboveBlock.type != Material.AIR) return null

        // Need a surface we can actually stand on
        if (!highestBlock.type.isSolid || highestBlock.type.isDangerous()) return null

        return Location(world, x.toDouble(), highestBlock.y + 1.0, z.toDouble())
            .toCenterLocation() // Don't put them on the corner of a block
    }
}

private fun Material.isDangerous(): Boolean {
    return this in listOf(
        Material.LAVA,
        Material.WATER,
        Material.KELP, // Underwater vegetation
        Material.KELP_PLANT, // Underwater vegetation
        Material.SEAGRASS, // Underwater vegetation
        Material.TALL_SEAGRASS, // Underwater vegetation
        Material.CACTUS,
        Material.VOID_AIR,
    )
}