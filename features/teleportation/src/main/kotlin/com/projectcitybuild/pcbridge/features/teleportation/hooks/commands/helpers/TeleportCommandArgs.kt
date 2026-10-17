package com.projectcitybuild.pcbridge.features.teleportation.hooks.commands.helpers

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportFlags
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

private enum class TeleportFlag(
    val literal: String,
    val permission: PermissionNode,
    val apply: (TeleportFlags) -> TeleportFlags,
    val isSet: (TeleportFlags) -> Boolean,
) {
    FORCE(
        literal = "--force",
        permission = PermissionNode.TELEPORT_FORCE,
        apply = { it.copy(force = true) },
        isSet = { it.force },
    ),
    SILENT(
        literal = "--silent",
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
        .map { flag ->
            val updatedFlags = flag.apply(flags)
            Commands.literal(flag.literal)
                .requiresPermission(flag.permission)
                .executesSuspending(plugin) { action(it, updatedFlags) }
                .also { branch ->
                    flagBranches(plugin, updatedFlags, action).forEach(branch::then)
                }
        }
