package com.projectcitybuild.pcbridge.paper.runtime.permissions

class Permissions {
    private var permissionsProvider: PermissionsProvider = BasicPermissionsProvider()

    val provider: PermissionsProvider
        get() = permissionsProvider

    fun setProvider(provider: PermissionsProvider?) {
        permissionsProvider = provider ?: BasicPermissionsProvider()
    }
}
