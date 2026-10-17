package com.projectcitybuild.pcbridge.features.teleportation.hooks.helpers

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

internal fun silentFlag(
    plugin: Plugin,
    action: suspend (PaperCommandContext) -> Unit,
): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("--silent")
        .requiresPermission(PermissionNode.TELEPORT_SILENT)
        .executesSuspending(plugin, action)

internal fun forceFlag(
    plugin: Plugin,
    action: suspend (PaperCommandContext) -> Unit,
): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("--force")
        .requiresPermission(PermissionNode.TELEPORT_FORCE)
        .executesSuspending(plugin, action)
