package com.projectcitybuild.pcbridge.platform.paper.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import io.papermc.paper.command.brigadier.CommandSourceStack

fun LiteralArgumentBuilder<CommandSourceStack>.requiresPermission(
    vararg permissions: PermissionNode,
): LiteralArgumentBuilder<CommandSourceStack> = requires { context ->
    permissions.all { context.sender.hasPermission(it.node) }
}

fun <S : CommandSourceStack, T> RequiredArgumentBuilder<S, T>.requiresPermission(
    vararg permissions: PermissionNode,
): RequiredArgumentBuilder<S, T> = requires { context ->
    permissions.all { context.sender.hasPermission(it.node) }
}
