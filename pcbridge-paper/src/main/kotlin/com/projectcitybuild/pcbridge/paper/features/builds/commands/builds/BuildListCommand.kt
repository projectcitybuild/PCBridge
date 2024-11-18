package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.repositories.BuildRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildListCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
) {
    fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("list")
            .then(
                Commands.argument("page", IntegerArgumentType.integer())
                    .executes { context ->
                        plugin.launch {
                            execute(
                                sender = context.source.sender,
                                page = context.getArgument("page", Int::class.java),
                            )
                        }
                        com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
            )
            .build()
    }

    private suspend fun execute(sender: CommandSender, page: Int) {
        val builds = buildRepository.all(page)

        builds.data.forEach { data ->
            sender.sendMessage(
                MiniMessage.miniMessage().deserialize(data.name)
            )
        }
    }
}