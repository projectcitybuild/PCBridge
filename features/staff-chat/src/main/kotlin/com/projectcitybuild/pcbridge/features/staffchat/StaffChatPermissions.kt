package com.projectcitybuild.pcbridge.features.staffchat

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode

object StaffChatPermissions {
    // Can send and receive staff messages
    val CHANNEL = PermissionNode("pcbridge.chat.staff_channel")
}
