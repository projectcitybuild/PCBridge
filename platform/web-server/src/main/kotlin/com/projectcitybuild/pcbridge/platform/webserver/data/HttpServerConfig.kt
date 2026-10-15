package com.projectcitybuild.pcbridge.platform.webserver.data

data class HttpServerConfig(
    val host: String,
    val port: Int,
    val authToken: String,
)