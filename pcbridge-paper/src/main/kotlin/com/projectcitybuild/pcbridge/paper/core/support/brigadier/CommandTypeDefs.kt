package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack

typealias PaperCommandContext = CommandContext<CommandSourceStack>
typealias PaperCommandNode = LiteralCommandNode<CommandSourceStack>