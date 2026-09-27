package com.projectcitybuild.pcbridge.paper.platform.paper.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import io.papermc.paper.command.brigadier.CommandSourceStack

fun LiteralArgumentBuilder<CommandSourceStack>.requiresPermission(permission: PermissionNode): LiteralArgumentBuilder<CommandSourceStack> =
    requires { context ->
        context.sender.hasPermission(permission.node)
    }

fun <S : CommandSourceStack, T> RequiredArgumentBuilder<S, T>.requiresPermission(
    permission: PermissionNode,
): RequiredArgumentBuilder<S, T> =
    requires { context ->
        context.sender.hasPermission(permission.node)
    }
