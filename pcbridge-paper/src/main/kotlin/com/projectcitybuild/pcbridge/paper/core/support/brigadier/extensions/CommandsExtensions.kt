package com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions

import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import io.papermc.paper.command.brigadier.Commands

fun Commands.register(command: BrigadierCommand)
    = register(command.buildLiteral(), command.description)
