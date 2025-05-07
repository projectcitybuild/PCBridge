package com.projectcitybuild.pcbridge.paper.features.borders.commands.border

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.borders.actions.PlayerBorderCheck
import com.projectcitybuild.pcbridge.paper.features.borders.data.Border
import com.projectcitybuild.pcbridge.paper.features.borders.repositories.WorldBorderRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class BorderSetRectangleCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val worldBorderRepository: WorldBorderRepository,
    private val playerBorderCheck: PlayerBorderCheck,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("rectangle")
            .then(
                Commands.argument("minX", IntegerArgumentType.integer())
                    .then(
                        Commands.argument("minZ", IntegerArgumentType.integer())
                            .then(
                                Commands.argument("maxX", IntegerArgumentType.integer())
                                    .then(
                                        Commands.argument("maxZ", IntegerArgumentType.integer())
                                            .executesSuspending(plugin, ::execute)
                                    )
                            )
                    )
            )
            .build()
    }

    suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.sender as? Player
        checkNotNull(player) { "Only players can use this command" }

        val minX = context.getArgument("minX", Int::class.java).toDouble()
        val minZ = context.getArgument("minZ", Int::class.java).toDouble()
        val maxX = context.getArgument("maxX", Int::class.java).toDouble()
        val maxZ = context.getArgument("maxZ", Int::class.java).toDouble()

        check(minX <= maxX) { "Min x must be less than max x"}
        check(minZ <= maxZ) { "Min z must be less than max z"}
        check(maxX - minX > 10) { "X axis is too small" }
        check(maxZ - minZ > 10) { "Z axis is too small" }

        val border = Border.Rectangle(minX, minZ, maxX, maxZ)
        val world = player.location.world
        worldBorderRepository.set(world, border)

        player.sendRichMessage("<gray>World border for <yellow>${world.name}</yellow> set to <yellow>(${border.minX}, ${border.minZ}) - (${border.maxX}, ${border.maxZ})</yellow></gray>")

        server.onlinePlayers.forEach {
            playerBorderCheck.check(it)
        }
    }
}