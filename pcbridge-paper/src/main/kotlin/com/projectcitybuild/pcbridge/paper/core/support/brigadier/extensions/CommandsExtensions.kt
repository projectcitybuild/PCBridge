package com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions

import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import io.papermc.paper.command.brigadier.Commands

fun Commands.register(vararg commands: BrigadierCommand) = commands.forEach {
    register(it.buildLiteral())
}