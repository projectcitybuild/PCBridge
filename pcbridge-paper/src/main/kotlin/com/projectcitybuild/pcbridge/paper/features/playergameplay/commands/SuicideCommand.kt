package com.projectcitybuild.pcbridge.paper.features.playergameplay.commands

import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class SuicideCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode
        = Commands.literal("suicide")
            .executesSuspending(plugin, ::execute)
            .build()

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()
        player.health = 0.0
    }
}
