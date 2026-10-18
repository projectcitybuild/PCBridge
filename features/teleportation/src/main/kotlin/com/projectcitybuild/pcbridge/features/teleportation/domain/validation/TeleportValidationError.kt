package com.projectcitybuild.pcbridge.features.teleportation.domain.validation

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.core.validation.ValidationError

sealed interface TeleportValidationError : ValidationError {
    data class MissingPermission(
        val permission: PermissionNode,
    ) : TeleportValidationError

    data object TargetOffline : TeleportValidationError

    data object SelfTarget : TeleportValidationError

    data object ExplicitTargetRequired : TeleportValidationError

    data object InvalidCoordinates : TeleportValidationError
}
