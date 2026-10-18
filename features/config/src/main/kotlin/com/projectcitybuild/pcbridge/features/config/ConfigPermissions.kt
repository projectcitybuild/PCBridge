package com.projectcitybuild.pcbridge.features.config

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object ConfigPermissions {
    // Can force reload the remote config
    val RELOAD = PermissionNode("pcbridge.config.reload")
}
