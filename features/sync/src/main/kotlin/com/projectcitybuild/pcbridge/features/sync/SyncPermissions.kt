package com.projectcitybuild.pcbridge.features.sync

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object SyncPermissions {
    // Can force sync another player (in addition to yourself)
    val OTHER = PermissionNode("pcbridge.sync.other")
}
