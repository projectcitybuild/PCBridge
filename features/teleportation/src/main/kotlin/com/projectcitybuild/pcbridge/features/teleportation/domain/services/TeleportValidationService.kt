package com.projectcitybuild.pcbridge.features.teleportation.domain.services

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TeleportValidationService {
    fun to(
        requester: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ): Boolean {
        if (!requirePermission(requester, PermissionNode.TELEPORT_REQUEST)) {
            return false
        }
        if (flags.force && !requirePermission(requester, PermissionNode.TELEPORT_FORCE)) {
            return false
        }
        if (!requireSilentPermission(requester, flags.silent)) {
            return false
        }
        if (!requester.isOnline || !target.isOnline) {
            requester.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        if (requester.uniqueId == target.uniqueId) {
            requester.sendMessage(l10n.errorTeleportSelf)
            return false
        }
        return true
    }

    fun here(
        operator: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ): Boolean {
        if (!requirePermission(operator, PermissionNode.TELEPORT_OTHERS)) {
            return false
        }
        if (flags.force && !requirePermission(operator, PermissionNode.TELEPORT_FORCE)) {
            return false
        }
        if (!requireSilentPermission(operator, flags.silent)) {
            return false
        }
        if (!target.isOnline) {
            operator.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        if (!flags.force && operator.uniqueId == target.uniqueId) {
            operator.sendMessage(l10n.errorTeleportSelf)
            return false
        }
        return true
    }

    fun requestHere(
        requester: Player,
        target: Player,
    ): Boolean {
        if (!requirePermission(requester, PermissionNode.TELEPORT_REQUEST)) {
            return false
        }
        if (!requester.isOnline || !target.isOnline) {
            requester.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        if (requester.uniqueId == target.uniqueId) {
            requester.sendMessage(l10n.errorTeleportSelf)
            return false
        }
        return true
    }

    fun send(
        operator: CommandSender,
        player: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ): Boolean {
        if (!requirePermission(operator, PermissionNode.TELEPORT_OTHERS)) {
            return false
        }
        if (flags.force && !requirePermission(operator, PermissionNode.TELEPORT_FORCE)) {
            return false
        }
        if (!requireSilentPermission(operator, flags.silent)) {
            return false
        }
        if (!player.isOnline || !target.isOnline) {
            operator.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        if (player.uniqueId == target.uniqueId) {
            operator.sendMessage(l10n.errorTeleportSelf)
            return false
        }
        return true
    }

    fun all(
        operator: CommandSender,
        requestedTarget: Player?,
        flags: TeleportFlags = TeleportFlags(),
    ): Player? {
        val target = requestedTarget ?: operator as? Player

        if (!requirePermission(operator, PermissionNode.TELEPORT_ALL)) {
            return null
        }
        if (flags.force && !requirePermission(operator, PermissionNode.TELEPORT_FORCE)) {
            return null
        }
        if (!requireSilentPermission(operator, flags.silent)) {
            return null
        }
        if (target == null) {
            operator.sendMessage(l10n.errorTeleportExplicitTargetRequired)
            return null
        }
        if (!target.isOnline) {
            operator.sendMessage(l10n.errorTeleportTargetOffline)
            return null
        }
        return target
    }

    fun position(
        player: Player,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float? = null,
        pitch: Float? = null,
    ): Boolean {
        if (!requirePermission(player, PermissionNode.TELEPORT_POSITION)) {
            return false
        }
        if (!player.isOnline) {
            player.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        if (!x.isFinite() || !y.isFinite() || !z.isFinite()) {
            player.sendMessage(l10n.errorTeleportCoordinatesInvalid)
            return false
        }
        return true
    }

    fun random(player: Player): Boolean {
        if (!requirePermission(player, PermissionNode.TELEPORT_RANDOM)) {
            return false
        }
        if (!player.isOnline) {
            player.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        return true
    }

    fun back(
        operator: CommandSender,
        target: Player,
    ): Boolean {
        val permission = if ((operator as? Player)?.uniqueId == target.uniqueId) {
            PermissionNode.TELEPORT_BACK
        } else {
            PermissionNode.TELEPORT_BACK_OTHERS
        }
        if (!requirePermission(operator, permission)) {
            return false
        }
        if (!target.isOnline) {
            operator.sendMessage(l10n.errorTeleportTargetOffline)
            return false
        }
        return true
    }

    private fun requireSilentPermission(
        sender: CommandSender,
        silent: Boolean,
    ): Boolean = !silent || requirePermission(sender, PermissionNode.TELEPORT_SILENT)

    private fun requirePermission(
        sender: CommandSender,
        permission: PermissionNode,
    ): Boolean {
        if (sender.hasPermission(permission.node)) return true
        sender.sendMessage(l10n.errorTeleportPermission)
        return false
    }
}
