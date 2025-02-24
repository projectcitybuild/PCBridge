package com.projectcitybuild.pcbridge.paper.architecture.connection.middleware

import net.kyori.adventure.text.Component

sealed class ConnectionResult {
    data object Allowed: ConnectionResult()
    data class Denied(val reason: Component): ConnectionResult()
}