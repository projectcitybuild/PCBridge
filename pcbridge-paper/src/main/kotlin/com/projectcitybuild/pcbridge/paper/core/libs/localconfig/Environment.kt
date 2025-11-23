package com.projectcitybuild.pcbridge.paper.core.libs.localconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Environment(val rawValue: String) {
    @SerialName("dev")
    DEV("dev"),

    @SerialName("production")
    PRODUCTION("production"),
    ;

    val isProduction: Boolean
        get() = this == PRODUCTION
}