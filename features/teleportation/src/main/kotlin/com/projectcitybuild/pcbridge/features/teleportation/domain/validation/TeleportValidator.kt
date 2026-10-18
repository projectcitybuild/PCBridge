package com.projectcitybuild.pcbridge.features.teleportation.domain.validation

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.core.validation.ValidationResult
import com.projectcitybuild.pcbridge.features.teleportation.domain.data.TeleportFlags
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TeleportValidator {
    fun to(
        requester: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ): ValidationResult<Unit> {
        permission(requester, PermissionNode.TELEPORT_REQUEST)?.let {
            return invalid(it)
        }
        if (flags.force) {
            permission(requester, PermissionNode.TELEPORT_FORCE)?.let {
                return invalid(it)
            }
        }
        silentPermission(requester, flags.silent)?.let {
            return invalid(it)
        }
        if (!requester.isOnline || !target.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        if (requester.uniqueId == target.uniqueId) {
            return invalid(TeleportValidationError.SelfTarget)
        }
        return valid()
    }

    fun here(
        operator: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ): ValidationResult<Unit> {
        permission(operator, PermissionNode.TELEPORT_OTHERS)?.let {
            return invalid(it)
        }
        if (flags.force) {
            permission(operator, PermissionNode.TELEPORT_FORCE)?.let {
                return invalid(it)
            }
        }
        silentPermission(operator, flags.silent)?.let {
            return invalid(it)
        }
        if (!target.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        if (operator.uniqueId == target.uniqueId) {
            return invalid(TeleportValidationError.SelfTarget)
        }
        return valid()
    }

    fun requestHere(
        requester: Player,
        target: Player,
    ): ValidationResult<Unit> {
        permission(requester, PermissionNode.TELEPORT_REQUEST)?.let {
            return invalid(it)
        }
        if (!requester.isOnline || !target.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        if (requester.uniqueId == target.uniqueId) {
            return invalid(TeleportValidationError.SelfTarget)
        }
        return valid()
    }

    fun send(
        operator: CommandSender,
        player: Player,
        target: Player,
        flags: TeleportFlags = TeleportFlags(),
    ): ValidationResult<Unit> {
        permission(operator, PermissionNode.TELEPORT_OTHERS)?.let {
            return invalid(it)
        }
        if (flags.force) {
            permission(operator, PermissionNode.TELEPORT_FORCE)?.let {
                return invalid(it)
            }
        }
        silentPermission(operator, flags.silent)?.let {
            return invalid(it)
        }
        if (!player.isOnline || !target.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        if (player.uniqueId == target.uniqueId) {
            return invalid(TeleportValidationError.SelfTarget)
        }
        return valid()
    }

    /**
     * Resolves the player to teleport for the "all" operation.
     *
     * If no target was explicitly provided, a player executing the
     * command becomes the target. Console senders must provide one.
     */
    fun all(
        operator: CommandSender,
        requestedTarget: Player?,
        flags: TeleportFlags = TeleportFlags(),
    ): ValidationResult<Player> {
        permission(operator, PermissionNode.TELEPORT_ALL)?.let {
            return invalid(it)
        }
        if (flags.force) {
            permission(operator, PermissionNode.TELEPORT_FORCE)?.let {
                return invalid(it)
            }
        }
        silentPermission(operator, flags.silent)?.let {
            return invalid(it)
        }
        val target = requestedTarget ?: operator as? Player
            ?: return invalid(
                TeleportValidationError.ExplicitTargetRequired,
            )
        if (!target.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        return ValidationResult.Valid(target)
    }

    fun position(
        player: Player,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float? = null,
        pitch: Float? = null,
    ): ValidationResult<Unit> {
        permission(player, PermissionNode.TELEPORT_POSITION)?.let {
            return invalid(it)
        }
        if (!player.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        if (
            !x.isFinite() ||
            !y.isFinite() ||
            !z.isFinite() ||
            (yaw != null && !yaw.isFinite()) ||
            (pitch != null && !pitch.isFinite())
        ) {
            return invalid(TeleportValidationError.InvalidCoordinates)
        }
        return valid()
    }

    fun random(player: Player): ValidationResult<Unit> {
        permission(player, PermissionNode.TELEPORT_RANDOM)?.let {
            return invalid(it)
        }
        if (!player.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        return valid()
    }

    fun back(
        sender: CommandSender,
        target: Player,
    ): ValidationResult<Unit> {
        val senderPlayer = sender as? Player
        val requiredPermission = if (senderPlayer?.uniqueId == target.uniqueId) {
            PermissionNode.TELEPORT_BACK
        } else {
            PermissionNode.TELEPORT_BACK_OTHERS
        }
        permission(sender, requiredPermission)?.let {
            return invalid(it)
        }
        if (!target.isOnline) {
            return invalid(TeleportValidationError.TargetOffline)
        }
        return valid()
    }

    private fun silentPermission(
        sender: CommandSender,
        silent: Boolean,
    ): TeleportValidationError.MissingPermission? = if (silent) {
        permission(sender, PermissionNode.TELEPORT_SILENT)
    } else {
        null
    }

    private fun permission(
        sender: CommandSender,
        permission: PermissionNode,
    ): TeleportValidationError.MissingPermission? = if (sender.hasPermission(permission.node)) {
        null
    } else {
        TeleportValidationError.MissingPermission(permission)
    }

    private fun valid(): ValidationResult<Unit> = ValidationResult.Valid(Unit)

    private fun invalid(error: TeleportValidationError): ValidationResult.Invalid = ValidationResult.Invalid(listOf(error))
}
