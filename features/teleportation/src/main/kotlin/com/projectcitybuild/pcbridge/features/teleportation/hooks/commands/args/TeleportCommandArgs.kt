package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.args

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

private enum class TeleportFlag(
    val literal: String,
    val shorthand: String,
    val permission: PermissionNode,
    val apply: (TeleportFlags) -> TeleportFlags,
    val isSet: (TeleportFlags) -> Boolean,
) {
    FORCE(
        literal = "--force",
        shorthand = "-f",
        permission = PermissionNode.TELEPORT_FORCE,
        apply = { it.copy(force = true) },
        isSet = { it.force },
    ),
    SILENT(
        literal = "--silent",
        shorthand = "-s",
        permission = PermissionNode.TELEPORT_SILENT,
        apply = { it.copy(silent = true) },
        isSet = { it.silent }
    ),
}

internal fun LiteralArgumentBuilder<CommandSourceStack>.withTeleportFlags(
    plugin: Plugin,
    action: suspend (PaperCommandContext, TeleportFlags) -> Unit,
): LiteralArgumentBuilder<CommandSourceStack> {
    executesSuspending(plugin) { action(it, TeleportFlags()) }
    flagBranches(plugin, TeleportFlags(), action).forEach(::then)
    return this
}

internal fun <T> RequiredArgumentBuilder<CommandSourceStack, T>.withTeleportFlags(
    plugin: Plugin,
    action: suspend (PaperCommandContext, TeleportFlags) -> Unit,
): RequiredArgumentBuilder<CommandSourceStack, T> {
    executesSuspending(plugin) { action(it, TeleportFlags()) }
    flagBranches(plugin, TeleportFlags(), action).forEach(::then)
    return this
}

private fun flagBranches(
    plugin: Plugin,
    flags: TeleportFlags,
    action: suspend (PaperCommandContext, TeleportFlags) -> Unit,
): List<LiteralArgumentBuilder<CommandSourceStack>> =
    TeleportFlag.entries
        .filterNot { it.isSet(flags) }
        .flatMap { flag ->
            val updatedFlags = flag.apply(flags)

            listOf(flag.literal, flag.shorthand).map { literal ->
                Commands.literal(literal)
                    .requiresPermission(flag.permission)
                    .executesSuspending(plugin) { action(it, updatedFlags) }
                    .also { branch ->
                        flagBranches(plugin, updatedFlags, action).forEach(branch::then)
                    }
            }
        }
