package com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage

import com.projectcitybuild.pcbridge.paper.core.libs.localconfig.LocalConfig

@Deprecated("Move this to remote config")
class ManageUrlGenerator(
    private val localConfig: LocalConfig,
) {
    private val baseUrl: String get() {
        var baseUrl = localConfig.get().api.baseUrl
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
        return baseUrl.removeSuffix("api/")
    }

    fun path(path: String): String {
        return "${baseUrl}${path}"
    }
}