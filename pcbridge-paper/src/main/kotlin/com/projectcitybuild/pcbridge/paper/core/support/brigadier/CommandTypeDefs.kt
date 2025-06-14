package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack

typealias CommandContext = CommandContext<CommandSourceStack>
typealias CommandNode = LiteralCommandNode<CommandSourceStack>